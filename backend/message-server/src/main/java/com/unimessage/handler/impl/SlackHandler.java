package com.unimessage.handler.impl;

import cn.hutool.http.HttpUtil;
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
 * Slack 发送处理器 (Webhook方式)
 *
 * @author 海明
 * @since 2025-12-25
 */
@Slf4j
@Component
public class SlackHandler implements ChannelHandler {

    @Override
    public boolean support(String channelType) {
        return ChannelType.SLACK.getCode().equals(channelType);
    }

    @Override
    public boolean send(SysChannel channel, SysTemplate template, LogMsgDetail msgDetail, Map<String, Object> params) {
        log.info("开始发送Slack消息");

        try {
            JSONObject config = JSON.parseObject(channel.getConfigJson());
            String webhookUrl = config.getString("webhookUrl");

            if (webhookUrl == null || webhookUrl.isEmpty()) {
                throw new IllegalArgumentException("Slack配置缺失: webhookUrl 为空");
            }

            String content = renderContent(template.getContent(), params);
            msgDetail.setContent(content);

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("text", content);

            // Slack Webhook accepts JSON payload
            String response = HttpUtil.post(webhookUrl, JSON.toJSONString(paramMap));
            log.info("Slack响应: {}", response);

            if ("ok".equals(response)) {
                return true;
            } else {
                msgDetail.setErrorMsg(response);
                return false;
            }

        } catch (Exception e) {
            log.error("Slack发送异常", e);
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
