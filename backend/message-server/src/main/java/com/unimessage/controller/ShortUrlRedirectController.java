package com.unimessage.controller;

import com.unimessage.service.ShortUrlRateLimiterService;
import com.unimessage.service.ShortUrlService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * 短链接重定向控制器 (无需鉴权)
 *
 * @author 海明
 * @since 2026-01-14
 */
@Slf4j
@Controller
public class ShortUrlRedirectController {

    private static final Pattern SHORT_CODE_PATTERN = Pattern.compile("^[a-zA-Z0-9]{4,10}$");

    private static final String NOT_FOUND_PAGE = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>链接不存在</title>
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; 
                           display: flex; justify-content: center; align-items: center; height: 100vh; 
                           margin: 0; background: #f5f5f5; }
                    .container { text-align: center; padding: 40px; background: white; 
                                 border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    h1 { color: #333; margin-bottom: 10px; }
                    p { color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>404</h1>
                    <p>抱歉，该短链接不存在或已过期</p>
                </div>
            </body>
            </html>
            """;

    private static final String RATE_LIMIT_PAGE = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>访问受限</title>
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; 
                           display: flex; justify-content: center; align-items: center; height: 100vh; 
                           margin: 0; background: #f5f5f5; }
                    .container { text-align: center; padding: 40px; background: white; 
                                 border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    h1 { color: #e74c3c; margin-bottom: 10px; }
                    p { color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>429</h1>
                    <p>访问过于频繁，请稍后再试</p>
                </div>
            </body>
            </html>
            """;

    @Resource
    private ShortUrlService shortUrlService;

    @Resource
    private ShortUrlRateLimiterService rateLimiterService;

    /**
     * 短链接重定向
     * 使用 302 临时重定向，便于统计和更新目标URL
     */
    @GetMapping("/s/{shortCode}")
    public void redirect(@PathVariable String shortCode,
                         HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        // 0. 校验短链码格式
        if (shortCode == null || !SHORT_CODE_PATTERN.matcher(shortCode).matches()) {
            sendNotFoundPage(response);
            return;
        }

        String ip = getClientIp(request);
        if (ip == null || ip.isBlank()) {
            ip = "unknown";
        }

        // 1. 检查IP黑名单
        if (rateLimiterService.isIpBlacklisted(ip)) {
            log.warn("黑名单IP访问: ip={}, shortCode={}", ip, shortCode);
            sendRateLimitPage(response, HttpStatus.FORBIDDEN);
            return;
        }

        // 2. 检查IP限流 (先检查IP，减少全局计数器压力)
        if (!rateLimiterService.checkIpRateLimit(ip)) {
            log.warn("IP限流触发: ip={}, shortCode={}", ip, shortCode);
            rateLimiterService.recordRateLimitViolation(ip);
            sendRateLimitPage(response, HttpStatus.TOO_MANY_REQUESTS);
            return;
        }

        // 3. 检查全局限流
        if (!rateLimiterService.checkGlobalRateLimit()) {
            log.warn("全局限流触发: shortCode={}", shortCode);
            sendRateLimitPage(response, HttpStatus.TOO_MANY_REQUESTS);
            return;
        }

        // 4. 获取原始URL
        String originalUrl = shortUrlService.getOriginalUrl(shortCode);
        if (originalUrl == null) {
            log.debug("短链接不存在: shortCode={}", shortCode);
            sendNotFoundPage(response);
            return;
        }

        // 5. 安全校验：防止开放重定向攻击
        if (!isValidRedirectUrl(originalUrl)) {
            log.warn("非法重定向URL: shortCode={}, url={}", shortCode, originalUrl);
            sendNotFoundPage(response);
            return;
        }

        // 6. 异步记录访问日志
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");
        shortUrlService.recordAccess(shortCode, ip, userAgent, referer);

        // 7. 302重定向 (禁止缓存)
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        response.setStatus(HttpStatus.FOUND.value());
        response.sendRedirect(originalUrl);
    }

    /**
     * 校验重定向URL是否安全
     */
    private boolean isValidRedirectUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        // 只允许 http/https 协议
        String lowerUrl = url.toLowerCase();
        if (!lowerUrl.startsWith("http://") && !lowerUrl.startsWith("https://")) {
            return false;
        }
        // 禁止 javascript: 等伪协议
        if (lowerUrl.contains("javascript:") || lowerUrl.contains("data:") || lowerUrl.contains("vbscript:")) {
            return false;
        }
        return true;
    }

    private void sendNotFoundPage(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(NOT_FOUND_PAGE);
    }

    private void sendRateLimitPage(HttpServletResponse response, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Retry-After", "60");
        response.getWriter().write(RATE_LIMIT_PAGE);
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
