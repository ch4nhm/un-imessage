package com.unimessage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 短链接服务配置
 *
 * @author 海明
 * @since 2026-01-14
 */
@Data
@Component
@ConfigurationProperties(prefix = "un-imessage.short-url")
public class ShortUrlProperties {

    /**
     * 短链接域名 (用于生成完整短链)
     */
    private String domain = "http://localhost:8079";

    /**
     * 短链码长度
     */
    private int codeLength = 6;

    /**
     * 默认有效期(秒), 0表示永不过期
     */
    private long defaultTtl = 0;

    /**
     * 限流配置
     */
    private RateLimit rateLimit = new RateLimit();

    /**
     * 自动封禁配置
     */
    private AutoBan autoBan = new AutoBan();

    @Data
    public static class RateLimit {
        /**
         * 是否启用限流
         */
        private boolean enabled = true;

        /**
         * 单IP每分钟最大访问次数
         */
        private int maxRequestsPerMinute = 100;

        /**
         * 全局每分钟最大访问次数
         */
        private int globalMaxRequestsPerMinute = 10000;
    }

    @Data
    public static class AutoBan {
        /**
         * 是否启用自动封禁
         */
        private boolean enabled = true;

        /**
         * 触发封禁的阈值 (超过限流次数)
         */
        private int threshold = 10;

        /**
         * 封禁时长(秒)
         */
        private long duration = 3600;
    }
}
