package com.unimessage.constant;

/**
 * Redis 缓存 Key 常量定义
 * 统一前缀: uni-message:
 *
 * @author 海明
 * @since 2026-01-14
 */
public final class CacheKeyConstants {

    /**
     * 统一前缀
     */
    public static final String PREFIX = "uni-message:";
    /**
     * 消息发送队列
     */
    public static final String MQ_SEND_QUEUE = PREFIX + "mq:send:queue";

    // ==================== 消息队列 ====================
    /**
     * 短链接缓存前缀 (后接 shortCode)
     */
    public static final String SHORT_URL = PREFIX + "short-url:";

    // ==================== 短链接服务 ====================
    /**
     * 短链接IP限流前缀 (后接 ip)
     */
    public static final String SHORT_URL_RATE_LIMIT_IP = PREFIX + "short-url:rate-limit:ip:";
    /**
     * 短链接全局限流
     */
    public static final String SHORT_URL_RATE_LIMIT_GLOBAL = PREFIX + "short-url:rate-limit:global";
    /**
     * 短链接IP黑名单前缀 (后接 ip)
     */
    public static final String SHORT_URL_BLACKLIST = PREFIX + "short-url:blacklist:";
    /**
     * 短链接IP违规计数前缀 (后接 ip)
     */
    public static final String SHORT_URL_VIOLATION = PREFIX + "short-url:violation:";
    /**
     * 模版限流前缀 (后接 appid:template_code)
     */
    public static final String RATE_LIMIT_TEMPLATE = PREFIX + "rate-limit:template:";

    // ==================== 通用限流 ====================

    private CacheKeyConstants() {
    }
}
