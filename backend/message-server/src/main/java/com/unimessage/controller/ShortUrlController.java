package com.unimessage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unimessage.common.Result;
import com.unimessage.config.ShortUrlProperties;
import com.unimessage.context.AppContext;
import com.unimessage.dto.ShortUrlCreateRequest;
import com.unimessage.dto.ShortUrlResponse;
import com.unimessage.dto.ShortUrlStatsResponse;
import com.unimessage.entity.ShortUrl;
import com.unimessage.mapper.ShortUrlMapper;
import com.unimessage.service.ShortUrlService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 短链接管理控制器 (需要登录鉴权)
 *
 * @author 海明
 * @since 2026-01-14
 */
@RestController
@RequestMapping("/api/v1/short-url")
public class ShortUrlController {

    @Resource
    private ShortUrlService shortUrlService;

    @Resource
    private ShortUrlMapper shortUrlMapper;

    @Resource
    private ShortUrlProperties shortUrlProperties;

    /**
     * 分页查询短链接列表
     */
    @GetMapping("/page")
    public Result<IPage<ShortUrlResponse>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String shortCode,
            @RequestParam(required = false) String originalUrl,
            @RequestParam(required = false) Integer status) {
        Page<ShortUrl> page = new Page<>(current, size);
        LambdaQueryWrapper<ShortUrl> wrapper = new LambdaQueryWrapper<>();

        if (shortCode != null && !shortCode.isBlank()) {
            wrapper.like(ShortUrl::getShortCode, shortCode);
        }
        if (originalUrl != null && !originalUrl.isBlank()) {
            wrapper.like(ShortUrl::getOriginalUrl, originalUrl);
        }
        if (status != null) {
            wrapper.eq(ShortUrl::getStatus, status);
        }

        wrapper.orderByDesc(ShortUrl::getCreatedAt);
        Page<ShortUrl> resultPage = shortUrlMapper.selectPage(page, wrapper);

        // 转换为响应DTO
        IPage<ShortUrlResponse> responsePage = resultPage.convert(this::convertToResponse);
        return Result.success(responsePage);
    }

    /**
     * 创建短链接
     */
    @PostMapping
    public Result<ShortUrlResponse> create(@RequestBody ShortUrlCreateRequest request) {
        try {
            Long createdBy = AppContext.getCurrentAppId();
            ShortUrlResponse response = shortUrlService.createShortUrl(request, createdBy);
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取短链接详情
     */
    @GetMapping("/{shortCode}")
    public Result<ShortUrlResponse> getByCode(@PathVariable String shortCode) {
        var shortUrl = shortUrlService.getByShortCode(shortCode);
        if (shortUrl == null) {
            return Result.fail("短链接不存在");
        }
        return Result.success(convertToResponse(shortUrl));
    }

    /**
     * 获取短链接统计信息
     */
    @GetMapping("/{shortCode}/stats")
    public Result<ShortUrlStatsResponse> getStats(@PathVariable String shortCode) {
        ShortUrlStatsResponse stats = shortUrlService.getStats(shortCode);
        if (stats == null) {
            return Result.fail("短链接不存在");
        }
        return Result.success(stats);
    }

    /**
     * 启用短链接
     */
    @PutMapping("/{shortCode}/enable")
    public Result<Void> enable(@PathVariable String shortCode) {
        ShortUrl shortUrl = shortUrlService.getByShortCode(shortCode);
        if (shortUrl == null) {
            return Result.fail("短链接不存在");
        }
        shortUrl.setStatus(1);
        shortUrlMapper.updateById(shortUrl);
        return Result.success();
    }

    /**
     * 禁用短链接
     */
    @PutMapping("/{shortCode}/disable")
    public Result<Void> disable(@PathVariable String shortCode) {
        boolean success = shortUrlService.disableShortUrl(shortCode);
        return success ? Result.success() : Result.fail("操作失败");
    }

    /**
     * 删除短链接
     */
    @DeleteMapping("/{shortCode}")
    public Result<Void> delete(@PathVariable String shortCode) {
        boolean success = shortUrlService.deleteShortUrl(shortCode);
        return success ? Result.success() : Result.fail("操作失败");
    }

    /**
     * 转换为响应DTO
     */
    private ShortUrlResponse convertToResponse(ShortUrl shortUrl) {
        return ShortUrlResponse.builder()
                .shortCode(shortUrl.getShortCode())
                .shortUrl(shortUrlProperties.getDomain() + "/s/" + shortUrl.getShortCode())
                .originalUrl(shortUrl.getOriginalUrl())
                .clickCount(shortUrl.getClickCount())
                .expireAt(shortUrl.getExpireAt())
                .createdAt(shortUrl.getCreatedAt())
                .status(shortUrl.getStatus())
                .build();
    }
}
