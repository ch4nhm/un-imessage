package com.unimessage.sdk.client.impl;

import com.unimessage.dto.SendRequest;
import com.unimessage.dto.SendResponse;
import com.unimessage.dto.ShortUrlCreateRequest;
import com.unimessage.dto.ShortUrlResponse;
import com.unimessage.sdk.client.UniMessageClient;
import com.unimessage.sdk.config.UniMessageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * UniMessage 客户端默认实现
 *
 * @author 海明
 * @since 2025-12-08
 */
@Slf4j
public class UniMessageClientImpl implements UniMessageClient {

    private final UniMessageProperties properties;
    private final RestTemplate restTemplate;

    public UniMessageClientImpl(UniMessageProperties properties) {
        this.properties = properties;
        this.restTemplate = createRestTemplate();
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());
        return new RestTemplate(factory);
    }

    @Override
    public SendResponse send(SendRequest request) {
        // 如果没有提供 bizId，自动生成一个用于幂等性校验
        if (request.getBizId() == null || request.getBizId().isEmpty()) {
            request.setBizId(java.util.UUID.randomUUID().toString().replace("-", ""));
        }
        String url = properties.getBaseUrl() + "/send";
        return executeWithRetry(url, request, SendResponse.class);
    }

    @Override
    public ShortUrlResponse createShortUrl(ShortUrlCreateRequest request) {
        String url = properties.getShortUrlBaseUrl();
        ShortUrlApiResponse response = executeWithRetry(url, request, ShortUrlApiResponse.class);
        if (response != null && response.getCode() == 200) {
            return response.getData();
        }
        String errorMsg = response != null ? response.getMessage() : "Unknown error";
        throw new RuntimeException("Failed to create short url: " + errorMsg);
    }

    private <T, R> R executeWithRetry(String url, T request, Class<R> responseType) {
        HttpHeaders headers = createHeaders();
        HttpEntity<T> entity = new HttpEntity<>(request, headers);

        int maxRetries = properties.getMaxRetries();
        long retryInterval = properties.getRetryInterval();
        int attempt = 0;
        Exception lastException = null;

        long startTime = System.currentTimeMillis();

        while (attempt <= maxRetries) {
            try {
                attempt++;
                R response = restTemplate.postForObject(url, entity, responseType);
                long duration = System.currentTimeMillis() - startTime;
                log.debug("UniMessage request success, duration={}ms, url={}", duration, url);
                return response;
            } catch (RestClientException e) {
                lastException = e;
                log.warn("UniMessage request failed (attempt {}/{}), error: {}", attempt, maxRetries + 1, e.getMessage());
                if (attempt <= maxRetries) {
                    try {
                        Thread.sleep(retryInterval * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry", ie);
                    }
                }
            }
        }

        log.error("UniMessage request failed after {} attempts", attempt, lastException);
        throw new RuntimeException("Failed after " + attempt + " attempts", lastException);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (properties.getAppKey() != null) {
            headers.add("X-App-Key", properties.getAppKey());
        }
        if (properties.getAppSecret() != null) {
            headers.add("X-App-Secret", properties.getAppSecret());
        }
        return headers;
    }

    /**
     * 短链接API响应包装类
     */
    @lombok.Data
    private static class ShortUrlApiResponse {
        private Integer code;
        private String message;
        private ShortUrlResponse data;
    }
}
