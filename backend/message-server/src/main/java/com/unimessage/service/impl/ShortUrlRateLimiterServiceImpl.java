package com.unimessage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unimessage.cache.CacheService;
import com.unimessage.config.ShortUrlProperties;
import com.unimessage.constant.CacheKeyConstants;
import com.unimessage.entity.ShortUrlIpBlacklist;
import com.unimessage.mapper.ShortUrlIpBlacklistMapper;
import com.unimessage.service.ShortUrlRateLimiterService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 短链接限流服务实现
 * 基于Redis滑动窗口算法
 *
 * @author 海明
 * @since 2026-01-14
 */
@Slf4j
@Service
public class ShortUrlRateLimiterServiceImpl implements ShortUrlRateLimiterService {

    /**
     * 滑动窗口限流Lua脚本
     * KEYS[1]: 限流Key
     * ARGV[1]: 窗口大小(毫秒)
     * ARGV[2]: 最大请求数
     * ARGV[3]: 当前时间戳(毫秒)
     */
    private static final String SLIDING_WINDOW_SCRIPT =
            "local key = KEYS[1] " +
                    "local window = tonumber(ARGV[1]) " +
                    "local limit = tonumber(ARGV[2]) " +
                    "local now = tonumber(ARGV[3]) " +
                    "local clearBefore = now - window " +
                    "redis.call('ZREMRANGEBYSCORE', key, 0, clearBefore) " +
                    "local count = redis.call('ZCARD', key) " +
                    "if count < limit then " +
                    "    redis.call('ZADD', key, now, now .. '-' .. math.random()) " +
                    "    redis.call('EXPIRE', key, math.ceil(window / 1000) + 1) " +
                    "    return 1 " +
                    "else " +
                    "    return 0 " +
                    "end";

    private final DefaultRedisScript<Long> slidingWindowScript;

    @Resource
    private CacheService cacheService;

    @Resource
    private ShortUrlProperties properties;

    @Resource
    private ShortUrlIpBlacklistMapper blacklistMapper;

    public ShortUrlRateLimiterServiceImpl() {
        slidingWindowScript = new DefaultRedisScript<>();
        slidingWindowScript.setScriptText(SLIDING_WINDOW_SCRIPT);
        slidingWindowScript.setResultType(Long.class);
    }

    @Override
    public boolean checkIpRateLimit(String ip) {
        if (!properties.getRateLimit().isEnabled()) {
            return true;
        }

        String key = cacheService.buildKey(CacheKeyConstants.SHORT_URL_RATE_LIMIT_IP, ip);
        // 1分钟窗口
        long windowMs = 60 * 1000L;
        int limit = properties.getRateLimit().getMaxRequestsPerMinute();

        try {
            Long result = cacheService.getRedisUtil().execute(
                    slidingWindowScript,
                    Collections.singletonList(key),
                    String.valueOf(windowMs),
                    String.valueOf(limit),
                    String.valueOf(System.currentTimeMillis())
            );
            return result != null && result == 1L;
        } catch (Exception e) {
            log.error("IP限流检查失败", e);
            // 降级策略：Redis故障时放行
            return true;
        }
    }

    @Override
    public boolean checkGlobalRateLimit() {
        if (!properties.getRateLimit().isEnabled()) {
            return true;
        }

        long windowMs = 60 * 1000L;
        int limit = properties.getRateLimit().getGlobalMaxRequestsPerMinute();

        try {
            Long result = cacheService.getRedisUtil().execute(
                    slidingWindowScript,
                    Collections.singletonList(CacheKeyConstants.SHORT_URL_RATE_LIMIT_GLOBAL),
                    String.valueOf(windowMs),
                    String.valueOf(limit),
                    String.valueOf(System.currentTimeMillis())
            );
            return result != null && result == 1L;
        } catch (Exception e) {
            log.error("全局限流检查失败", e);
            return true;
        }
    }

    @Override
    public boolean isIpBlacklisted(String ip) {
        if (ip == null || ip.isBlank() || "unknown".equals(ip)) {
            return false;
        }

        try {
            // 1. 先检查Redis缓存
            String cacheKey = cacheService.buildKey(CacheKeyConstants.SHORT_URL_BLACKLIST, ip);
            String cached = cacheService.get(cacheKey);
            if ("1".equals(cached)) {
                return true;
            }
            if ("0".equals(cached)) {
                return false;
            }

            // 2. 查询数据库
            LambdaQueryWrapper<ShortUrlIpBlacklist> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShortUrlIpBlacklist::getIp, ip)
                    .and(w -> w.isNull(ShortUrlIpBlacklist::getExpireAt)
                            .or()
                            .gt(ShortUrlIpBlacklist::getExpireAt, LocalDateTime.now()));
            ShortUrlIpBlacklist blacklist = blacklistMapper.selectOne(wrapper);

            boolean isBlacklisted = blacklist != null;

            // 3. 缓存结果
            cacheService.set(cacheKey, isBlacklisted ? "1" : "0", 5, TimeUnit.MINUTES);

            return isBlacklisted;
        } catch (Exception e) {
            log.error("检查IP黑名单失败: ip={}", ip, e);
            // 降级策略：异常时放行，避免影响正常用户
            return false;
        }
    }

    @Override
    public void addToBlacklist(String ip, String reason, long duration) {
        if (ip == null || ip.isBlank() || "unknown".equals(ip)) {
            return;
        }

        try {
            // 1. 保存到数据库
            ShortUrlIpBlacklist blacklist = new ShortUrlIpBlacklist();
            blacklist.setIp(ip);
            blacklist.setReason(reason);
            blacklist.setExpireAt(duration > 0 ? LocalDateTime.now().plusSeconds(duration) : null);
            blacklist.setCreatedAt(LocalDateTime.now());

            // 先删除旧记录
            LambdaQueryWrapper<ShortUrlIpBlacklist> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(ShortUrlIpBlacklist::getIp, ip);
            blacklistMapper.delete(deleteWrapper);

            blacklistMapper.insert(blacklist);

            // 2. 更新Redis缓存
            String cacheKey = cacheService.buildKey(CacheKeyConstants.SHORT_URL_BLACKLIST, ip);
            if (duration > 0) {
                cacheService.set(cacheKey, "1", duration, TimeUnit.SECONDS);
            } else {
                cacheService.set(cacheKey, "1");
            }

            log.warn("IP已加入黑名单: ip={}, reason={}, duration={}s", ip, reason, duration);
        } catch (Exception e) {
            log.error("添加IP到黑名单失败: ip={}", ip, e);
        }
    }

    @Override
    public void removeFromBlacklist(String ip) {
        // 1. 从数据库删除
        LambdaQueryWrapper<ShortUrlIpBlacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortUrlIpBlacklist::getIp, ip);
        blacklistMapper.delete(wrapper);

        // 2. 删除Redis缓存
        cacheService.delete(cacheService.buildKey(CacheKeyConstants.SHORT_URL_BLACKLIST, ip));

        log.info("IP已从黑名单移除: ip={}", ip);
    }

    @Override
    public int recordRateLimitViolation(String ip) {
        if (ip == null || ip.isBlank() || "unknown".equals(ip)) {
            return 0;
        }

        try {
            String key = cacheService.buildKey(CacheKeyConstants.SHORT_URL_VIOLATION, ip);
            Long count = cacheService.increment(key, 1);
            if (count != null && count == 1) {
                // 首次违规，设置过期时间
                cacheService.expire(key, 10, TimeUnit.MINUTES);
            }

            int violationCount = count != null ? count.intValue() : 0;

            // 检查是否需要自动封禁
            if (properties.getAutoBan().isEnabled() && violationCount >= properties.getAutoBan().getThreshold()) {
                addToBlacklist(ip, "自动封禁: 频繁触发限流", properties.getAutoBan().getDuration());
                cacheService.delete(key);
            }

            return violationCount;
        } catch (Exception e) {
            log.error("记录限流违规失败: ip={}", ip, e);
            return 0;
        }
    }
}
