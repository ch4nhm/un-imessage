package com.unimessage.sdk.client;

import com.unimessage.dto.SendRequest;
import com.unimessage.dto.SendResponse;
import com.unimessage.dto.ShortUrlCreateRequest;
import com.unimessage.dto.ShortUrlResponse;

/**
 * UniMessage 客户端接口
 *
 * @author 海明
 * @since 2025-12-08
 */
public interface UniMessageClient {

    /**
     * 发送消息
     *
     * @param request 发送请求
     * @return 发送响应
     */
    SendResponse send(SendRequest request);

    /**
     * 创建短链接
     *
     * @param request 创建请求
     * @return 短链接响应
     */
    ShortUrlResponse createShortUrl(ShortUrlCreateRequest request);

    /**
     * 创建短链接 (简化方法)
     *
     * @param url 原始URL
     * @return 短链接响应
     */
    default ShortUrlResponse createShortUrl(String url) {
        ShortUrlCreateRequest request = new ShortUrlCreateRequest();
        request.setUrl(url);
        return createShortUrl(request);
    }

    /**
     * 创建短链接 (带有效期)
     *
     * @param url 原始URL
     * @param ttlSeconds 有效期(秒)
     * @return 短链接响应
     */
    default ShortUrlResponse createShortUrl(String url, long ttlSeconds) {
        ShortUrlCreateRequest request = new ShortUrlCreateRequest();
        request.setUrl(url);
        request.setTtl(ttlSeconds);
        return createShortUrl(request);
    }
}
