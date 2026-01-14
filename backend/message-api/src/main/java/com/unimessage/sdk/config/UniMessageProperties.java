package com.unimessage.sdk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * UniMessage 客户端配置
 *
 * @author 海明
 * @since 2025-12-08
 */
@Data
@ConfigurationProperties(prefix = "un-imessage.client")
public class UniMessageProperties {

    /**
     * 是否启用 UniMessage 客户端
     */
    private boolean enabled = true;

    /**
     * 服务端地址 (IP或域名)
     */
    private String host = "localhost";

    /**
     * 服务端端口
     */
    private Integer port = 8079;

    /**
     * 是否使用 HTTPS
     */
    private boolean useSsl = false;

    /**
     * 应用 Key (鉴权用)
     */
    private String appKey;

    /**
     * 应用 Secret (鉴权用)
     */
    private String appSecret;

    /**
     * 连接超时时间 (毫秒)
     */
    private int connectTimeout = 5000;

    /**
     * 读取超时时间 (毫秒)
     */
    private int readTimeout = 10000;

    /**
     * 最大重试次数
     */
    private int maxRetries = 3;

    /**
     * 重试间隔 (毫秒)
     */
    private long retryInterval = 100;

    /**
     * 获取完整的基础 URL
     */
    public String getBaseUrl() {
        String protocol = useSsl ? "https" : "http";
        return String.format("%s://%s:%d/api/v1/message", protocol, host, port);
    }

    /**
     * 获取短链接服务基础 URL
     */
    public String getShortUrlBaseUrl() {
        String protocol = useSsl ? "https" : "http";
        return String.format("%s://%s:%d/api/v1/short-url", protocol, host, port);
    }
}
