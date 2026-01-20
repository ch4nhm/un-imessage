package com.unimessage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.unimessage.cache.CacheService;
import com.unimessage.config.ShortUrlProperties;
import com.unimessage.constant.CacheKeyConstants;
import com.unimessage.dto.ShortUrlCreateRequest;
import com.unimessage.dto.ShortUrlResponse;
import com.unimessage.dto.ShortUrlStatsResponse;
import com.unimessage.entity.ShortUrl;
import com.unimessage.entity.ShortUrlAccessLog;
import com.unimessage.mapper.ShortUrlAccessLogMapper;
import com.unimessage.mapper.ShortUrlMapper;
import com.unimessage.service.ShortUrlService;
import com.unimessage.util.Base62Util;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 短链接服务实现
 *
 * @author 海明
 * @since 2026-01-14
 */
@Slf4j
@Service
public class ShortUrlServiceImpl implements ShortUrlService {

    /**
     * 支持 http/https, 域名/IP/localhost, 端口, 路径, 查询参数
     */
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)([\\w\\-]+\\.)*[\\w\\-]+(:\\d+)?(/[\\w\\-./?%&=@#]*)?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern SHORT_CODE_PATTERN = Pattern.compile("^[a-zA-Z0-9]{4,10}$");
    private static final int MAX_RETRY = 5;

    @Resource
    private ShortUrlMapper shortUrlMapper;

    @Resource
    private ShortUrlAccessLogMapper accessLogMapper;

    @Resource
    private CacheService cacheService;

    @Resource
    private ShortUrlProperties properties;

    @Override
    public ShortUrlResponse createShortUrl(ShortUrlCreateRequest request, Long createdBy) {
        // 1. 校验URL有效性
        if (request.getUrl() == null || request.getUrl().isBlank()) {
            throw new IllegalArgumentException("URL不能为空");
        }
        if (!isValidUrl(request.getUrl())) {
            throw new IllegalArgumentException("URL格式无效");
        }

        // 2. 检查是否已存在相同URL的短链
        ShortUrl existing = findByOriginalUrl(request.getUrl());
        if (existing != null && existing.getStatus() == 1) {
            return buildResponse(existing);
        }

        // 3. 生成短链码
        String shortCode;
        if (request.getCustomCode() != null && !request.getCustomCode().isBlank()) {
            shortCode = request.getCustomCode().trim();
            // 校验格式
            if (!SHORT_CODE_PATTERN.matcher(shortCode).matches()) {
                throw new IllegalArgumentException("自定义短链码只能包含字母和数字，长度4-10位");
            }
            if (getByShortCode(shortCode) != null) {
                throw new IllegalArgumentException("该短链码已被使用");
            }
        } else {
            shortCode = generateUniqueCode();
        }

        // 4. 计算过期时间
        LocalDateTime expireAt = null;
        long ttl = request.getTtl() != null ? request.getTtl() : properties.getDefaultTtl();
        if (ttl > 0) {
            expireAt = LocalDateTime.now().plusSeconds(ttl);
        }

        // 5. 保存到数据库 (利用唯一索引防止并发重复)
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode(shortCode);
        shortUrl.setOriginalUrl(request.getUrl());
        shortUrl.setCreatedBy(createdBy);
        shortUrl.setClickCount(0L);
        shortUrl.setStatus(1);
        shortUrl.setExpireAt(expireAt);
        shortUrl.setCreatedAt(LocalDateTime.now());

        try {
            shortUrlMapper.insert(shortUrl);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 并发冲突，重新生成
            log.warn("短链码冲突，重新生成: {}", shortCode);
            return createShortUrl(request, createdBy);
        }

        // 6. 缓存到Redis
        cacheShortUrl(shortUrl);

        return buildResponse(shortUrl);
    }

    @Override
    public String getOriginalUrl(String shortCode) {
        // 1. 先从Redis缓存获取
        String cacheKey = cacheService.buildKey(CacheKeyConstants.SHORT_URL, shortCode);
        String cachedUrl = cacheService.get(cacheKey);
        if (cachedUrl != null) {
            if ("NULL".equals(cachedUrl)) {
                return null;
            }
            return cachedUrl;
        }

        // 2. 从数据库获取
        ShortUrl shortUrl = getByShortCode(shortCode);
        if (shortUrl == null || shortUrl.getStatus() != 1) {
            // 缓存空值防止缓存穿透
            cacheService.set(cacheKey, "NULL", 5, TimeUnit.MINUTES);
            return null;
        }

        // 3. 检查是否过期
        if (shortUrl.getExpireAt() != null && shortUrl.getExpireAt().isBefore(LocalDateTime.now())) {
            cacheService.set(cacheKey, "NULL", 5, TimeUnit.MINUTES);
            return null;
        }

        // 4. 缓存并返回
        cacheShortUrl(shortUrl);
        return shortUrl.getOriginalUrl();
    }

    @Override
    public ShortUrl getByShortCode(String shortCode) {
        // 校验短链码格式，防止SQL注入
        if (shortCode == null || !SHORT_CODE_PATTERN.matcher(shortCode).matches()) {
            return null;
        }
        LambdaQueryWrapper<ShortUrl> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortUrl::getShortCode, shortCode);
        return shortUrlMapper.selectOne(wrapper);
    }

    @Override
    @Async("shortUrlAsyncExecutor")
    public void recordAccess(String shortCode, String ip, String userAgent, String referer) {
        try {
            // 1. 增加点击量
            shortUrlMapper.incrementClickCount(shortCode);

            // 2. 记录访问日志
            ShortUrlAccessLog accessLog = new ShortUrlAccessLog();
            accessLog.setShortCode(shortCode);
            accessLog.setIp(ip);
            accessLog.setUserAgent(truncate(userAgent, 500));
            accessLog.setReferer(truncate(referer, 500));
            accessLog.setAccessTime(LocalDateTime.now());
            accessLogMapper.insert(accessLog);
        } catch (Exception e) {
            log.error("记录访问日志失败: shortCode={}, ip={}", shortCode, ip, e);
        }
    }

    @Override
    public ShortUrlStatsResponse getStats(String shortCode) {
        ShortUrl shortUrl = getByShortCode(shortCode);
        if (shortUrl == null) {
            return null;
        }

        // 查询今日点击量
        LambdaQueryWrapper<ShortUrlAccessLog> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.eq(ShortUrlAccessLog::getShortCode, shortCode)
                .ge(ShortUrlAccessLog::getAccessTime, LocalDateTime.now().toLocalDate().atStartOfDay());
        Long todayClicks = accessLogMapper.selectCount(todayWrapper);

        // 查询最近10条访问记录
        LambdaQueryWrapper<ShortUrlAccessLog> recentWrapper = new LambdaQueryWrapper<>();
        recentWrapper.eq(ShortUrlAccessLog::getShortCode, shortCode)
                .orderByDesc(ShortUrlAccessLog::getAccessTime)
                .last("LIMIT 10");
        List<ShortUrlAccessLog> recentLogs = accessLogMapper.selectList(recentWrapper);

        List<ShortUrlStatsResponse.AccessRecord> recentAccess = recentLogs.stream()
                .map(log -> ShortUrlStatsResponse.AccessRecord.builder()
                        .ip(log.getIp())
                        .userAgent(log.getUserAgent())
                        .accessTime(log.getAccessTime())
                        .build())
                .collect(Collectors.toList());

        return ShortUrlStatsResponse.builder()
                .shortCode(shortCode)
                .originalUrl(shortUrl.getOriginalUrl())
                .totalClicks(shortUrl.getClickCount())
                .todayClicks(todayClicks)
                .createdAt(shortUrl.getCreatedAt())
                .recentAccess(recentAccess)
                .build();
    }

    @Override
    public boolean disableShortUrl(String shortCode) {
        LambdaUpdateWrapper<ShortUrl> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ShortUrl::getShortCode, shortCode)
                .set(ShortUrl::getStatus, 0);
        int rows = shortUrlMapper.update(null, wrapper);

        if (rows > 0) {
            cacheService.delete(cacheService.buildKey(CacheKeyConstants.SHORT_URL, shortCode));
        }
        return rows > 0;
    }

    @Override
    public boolean deleteShortUrl(String shortCode) {
        LambdaQueryWrapper<ShortUrl> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortUrl::getShortCode, shortCode);
        int rows = shortUrlMapper.delete(wrapper);

        if (rows > 0) {
            cacheService.delete(cacheService.buildKey(CacheKeyConstants.SHORT_URL, shortCode));
        }
        return rows > 0;
    }

    // ==================== 私有方法 ====================

    private boolean isValidUrl(String url) {
        return URL_PATTERN.matcher(url).matches();
    }

    private ShortUrl findByOriginalUrl(String originalUrl) {
        LambdaQueryWrapper<ShortUrl> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortUrl::getOriginalUrl, originalUrl)
                .eq(ShortUrl::getStatus, 1)
                .orderByDesc(ShortUrl::getCreatedAt)
                .last("LIMIT 1");
        return shortUrlMapper.selectOne(wrapper);
    }

    private String generateUniqueCode() {
        for (int i = 0; i < MAX_RETRY; i++) {
            String code = Base62Util.randomCode(properties.getCodeLength());
            if (getByShortCode(code) == null) {
                return code;
            }
            log.debug("短链码冲突，重试第{}次: {}", i + 1, code);
        }
        // 如果随机生成冲突，使用雪花算法思路：时间戳后几位 + 随机数，确保长度不超过10位
        // 取后8位数字编码
        String timestamp = Base62Util.encode(System.currentTimeMillis() % 100000000L);
        String random = Base62Util.randomCode(2);
        String fallbackCode = timestamp + random;
        // 确保长度在4-10之间
        if (fallbackCode.length() > 10) {
            fallbackCode = fallbackCode.substring(0, 10);
        }
        log.info("使用降级方案生成短链码: {}", fallbackCode);
        return fallbackCode;
    }

    private void cacheShortUrl(ShortUrl shortUrl) {
        String cacheKey = cacheService.buildKey(CacheKeyConstants.SHORT_URL, shortUrl.getShortCode());
        if (shortUrl.getExpireAt() != null) {
            long ttl = java.time.Duration.between(LocalDateTime.now(), shortUrl.getExpireAt()).getSeconds();
            if (ttl > 0) {
                cacheService.set(cacheKey, shortUrl.getOriginalUrl(), ttl, TimeUnit.SECONDS);
            }
        } else {
            // 永不过期的短链，缓存24小时
            cacheService.set(cacheKey, shortUrl.getOriginalUrl(), 24, TimeUnit.HOURS);
        }
    }

    private ShortUrlResponse buildResponse(ShortUrl shortUrl) {
        return ShortUrlResponse.builder()
                .shortCode(shortUrl.getShortCode())
                .shortUrl(properties.getDomain() + "/s/" + shortUrl.getShortCode())
                .originalUrl(shortUrl.getOriginalUrl())
                .clickCount(shortUrl.getClickCount())
                .expireAt(shortUrl.getExpireAt())
                .createdAt(shortUrl.getCreatedAt())
                .build();
    }

    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }
}
