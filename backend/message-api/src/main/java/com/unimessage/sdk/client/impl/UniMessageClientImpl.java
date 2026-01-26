package com.unimessage.sdk.client.impl;

import com.alibaba.fastjson2.JSON;
import com.unimessage.dto.*;
import com.unimessage.sdk.client.UniMessageClient;
import com.unimessage.sdk.config.UniMessageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
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
        try {
            ShortUrlApiResponse response = executeWithRetry(url, request, ShortUrlApiResponse.class);
            
            if (response == null) {
                throw new RuntimeException("Failed to create short url: response is null");
            }
            
            log.debug("ShortUrl API response: code={}, message={}, data={}", 
                    response.getCode(), response.getMessage(), response.getData());
            
            if (response.getCode() != null && response.getCode() == 200 && response.getData() != null) {
                return response.getData();
            }
            
            String errorMsg = response.getMessage() != null ? response.getMessage() : "Unknown error";
            throw new RuntimeException("Failed to create short url: " + errorMsg + " (code: " + response.getCode() + ")");
        } catch (Exception e) {
            log.error("Failed to create short url, url={}, request={}", url, request, e);
            throw new RuntimeException("Failed to create short url: " + e.getMessage(), e);
        }
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
                log.debug("UniMessage request attempt {}/{}, url={}, request={}", 
                        attempt, maxRetries + 1, url, request);
                
                // 使用 String 接收响应，然后用 Fastjson 解析
                ResponseEntity<String> responseEntity = restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        String.class
                );
                
                String responseBody = responseEntity.getBody();
                long duration = System.currentTimeMillis() - startTime;
                
                log.debug("UniMessage request success, duration={}ms, url={}, statusCode={}, responseBody={}", 
                        duration, url, responseEntity.getStatusCode(), responseBody);
                
                // 使用 Fastjson 解析 JSON 字符串
                if (responseBody == null || responseBody.isEmpty()) {
                    throw new RuntimeException("Response body is empty");
                }
                
                R response = JSON.parseObject(responseBody, responseType);
                log.debug("Parsed response: {}", response);
                
                return response;
            } catch (RestClientException e) {
                lastException = e;
                log.warn("UniMessage request failed (attempt {}/{}), url={}, error: {}", 
                        attempt, maxRetries + 1, url, e.getMessage());
                
                // 记录详细的错误信息
                if (e.getCause() != null) {
                    log.debug("Caused by: {}", e.getCause().getMessage(), e.getCause());
                }
                
                if (attempt <= maxRetries) {
                    try {
                        long sleepTime = retryInterval * attempt;
                        log.debug("Retrying after {}ms...", sleepTime);
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry", ie);
                    }
                }
            } catch (Exception e) {
                // 捕获 JSON 解析异常
                lastException = e;
                log.error("Failed to parse response, url={}, error: {}", url, e.getMessage(), e);
                
                if (attempt <= maxRetries) {
                    try {
                        long sleepTime = retryInterval * attempt;
                        log.debug("Retrying after {}ms...", sleepTime);
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry", ie);
                    }
                } else {
                    break;
                }
            }
        }

        log.error("UniMessage request failed after {} attempts, url={}", attempt, url, lastException);
        throw new RuntimeException("Failed after " + attempt + " attempts: " + lastException.getMessage(), lastException);
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
     * 用于反序列化服务端返回的 Result<ShortUrlResponse> 结构
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ShortUrlApiResponse {
        private Integer code;
        private String message;
        private ShortUrlResponse data;
    }
}
