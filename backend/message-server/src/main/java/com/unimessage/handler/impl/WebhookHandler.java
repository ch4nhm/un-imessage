package com.unimessage.handler.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.unimessage.entity.LogMsgDetail;
import com.unimessage.entity.SysChannel;
import com.unimessage.entity.SysTemplate;
import com.unimessage.enums.ChannelType;
import com.unimessage.handler.ChannelHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Webhook 通用发送处理器
 *
 * @author 海明
 * @since 2025-12-25
 */
@Slf4j
@Component
public class WebhookHandler implements ChannelHandler {

    @Override
    public boolean support(String channelType) {
        return ChannelType.WEBHOOK.getCode().equals(channelType);
    }

    @Override
    public boolean send(SysChannel channel, SysTemplate template, LogMsgDetail msgDetail, Map<String, Object> params) {
        log.info("开始发送Webhook: recipient={}", msgDetail.getRecipient());

        try {
            JSONObject config = JSON.parseObject(channel.getConfigJson());
            String url = config.getString("url");
            String method = config.getString("method"); // GET or POST
            JSONObject headers = config.getJSONObject("headers");

            if (url == null || url.isEmpty()) {
                throw new IllegalArgumentException("Webhook配置缺失: url 为空");
            }

            // Construct payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("recipient", msgDetail.getRecipient());
            payload.put("templateCode", template.getCode());
            payload.put("params", params);

            String content = renderContent(template.getContent(), params);
            payload.put("content", content);

            msgDetail.setContent(JSON.toJSONString(payload));

            HttpRequest request = "GET".equalsIgnoreCase(method) ? HttpRequest.get(url) : HttpRequest.post(url);

            if (headers != null) {
                for (Map.Entry<String, Object> entry : headers.entrySet()) {
                    request.header(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }

            // If POST, send JSON
            if (!"GET".equalsIgnoreCase(method)) {
                request.body(JSON.toJSONString(payload));
            }

            HttpResponse response = request.execute();
            String body = response.body();
            log.info("Webhook响应: status={}, body={}", response.getStatus(), body);

            if (response.isOk()) {
                return true;
            } else {
                msgDetail.setErrorMsg("Status: " + response.getStatus() + ", Body: " + body);
                return false;
            }

        } catch (Exception e) {
            log.error("Webhook发送异常", e);
            msgDetail.setErrorMsg(e.getMessage());
            return false;
        }
    }

    private String renderContent(String template, Map<String, Object> params) {
        if (template == null) return "";
        String content = template;
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = "${" + entry.getKey() + "}";
                if (content.contains(key)) {
                    content = content.replace(key, String.valueOf(entry.getValue()));
                }
            }
        }
        return content;
    }
}
