package com.unimessage.service;

/**
 * 短链接限流服务接口
 *
 * @author 海明
 * @since 2026-01-14
 */
public interface ShortUrlRateLimiterService {

    /**
     * 检查IP是否被限流
     *
     * @param ip IP地址
     * @return true: 允许访问, false: 被限流
     */
    boolean checkIpRateLimit(String ip);

    /**
     * 检查全局是否被限流
     *
     * @return true: 允许访问, false: 被限流
     */
    boolean checkGlobalRateLimit();

    /**
     * 检查IP是否在黑名单中
     *
     * @param ip IP地址
     * @return true: 在黑名单中, false: 不在
     */
    boolean isIpBlacklisted(String ip);

    /**
     * 将IP加入黑名单
     *
     * @param ip       IP地址
     * @param reason   封禁原因
     * @param duration 封禁时长(秒), 0表示永久
     */
    void addToBlacklist(String ip, String reason, long duration);

    /**
     * 将IP从黑名单移除
     *
     * @param ip IP地址
     */
    void removeFromBlacklist(String ip);

    /**
     * 记录IP访问次数 (用于自动封禁判断)
     *
     * @param ip IP地址
     * @return 当前窗口内被限流的次数
     */
    int recordRateLimitViolation(String ip);
}
