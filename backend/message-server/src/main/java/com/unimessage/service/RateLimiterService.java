package com.unimessage.service;

/**
 * 限流服务
 *
 * @author 海明
 * @since 2025-12-25
 */
public interface RateLimiterService {

    /**
     * 尝试获取许可
     *
     * @param key           限流资源Key
     * @param limit         限制数量
     * @param windowSeconds 时间窗口(秒)
     * @return true: 允许通过, false: 被限流
     */
    boolean tryAcquire(String key, int limit, int windowSeconds);
}
