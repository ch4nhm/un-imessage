package com.unimessage.service.impl;

import com.unimessage.cache.CacheService;
import com.unimessage.service.RateLimiterService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 限流服务实现
 *
 * @author Trae
 * @since 2025-12-25
 */
@Slf4j
@Service
public class RateLimiterServiceImpl implements RateLimiterService {

    /**
     * Lua脚本实现固定窗口限流
     * KEYS[1]: 限流Key
     * ARGV[1]: 阈值
     * ARGV[2]: 过期时间(秒)
     */
    private static final String LUA_SCRIPT =
            "local key = KEYS[1] " +
                    "local limit = tonumber(ARGV[1]) " +
                    "local window = tonumber(ARGV[2]) " +
                    "local current = redis.call('INCR', key) " +
                    "if current == 1 then " +
                    "    redis.call('EXPIRE', key, window) " +
                    "end " +
                    "if current > limit then " +
                    "    return 0 " +
                    "else " +
                    "    return 1 " +
                    "end";
    private final DefaultRedisScript<Long> redisScript;

    @Resource
    private CacheService cacheService;

    public RateLimiterServiceImpl() {
        redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(LUA_SCRIPT);
        redisScript.setResultType(Long.class);
    }

    @Override
    public boolean tryAcquire(String key, int limit, int windowSeconds) {
        if (limit <= 0) {
            return true;
        }
        try {
            Long result = cacheService.getRedisUtil().execute(redisScript, Collections.singletonList(key), String.valueOf(limit), String.valueOf(windowSeconds));
            return result != null && result == 1L;
        } catch (Exception e) {
            log.error("Rate limit check failed", e);
            // 降级策略：如果Redis挂了，默认放行，避免业务不可用
            return true;
        }
    }
}
