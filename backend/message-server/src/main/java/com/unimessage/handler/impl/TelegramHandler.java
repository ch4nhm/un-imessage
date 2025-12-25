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
 * Telegram 发送处理器
 *
 * @author 海明
 * @since 2025-12-25
 */
@Slf4j
@Component
public class TelegramHandler implements ChannelHandler {

    @Override
    public boolean support(String channelType) {
        return ChannelType.TELEGRAM.getCode().equals(channelType);
    }

    @Override
    public boolean send(SysChannel channel, SysTemplate template, LogMsgDetail msgDetail, Map<String, Object> params) {
        log.info("开始发送Telegram消息: recipient={}", msgDetail.getRecipient());

        try {
            JSONObject config = JSON.parseObject(channel.getConfigJson());
            String botToken = config.getString("botToken");

            if (botToken == null || botToken.isEmpty()) {
                throw new IllegalArgumentException("Telegram配置缺失: botToken 为空");
            }

            String content = renderContent(template.getContent(), params);
            msgDetail.setContent(content);

            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("chat_id", msgDetail.getRecipient());
            paramMap.put("text", content);

            String response = HttpUtil.post(url, paramMap);
            log.info("Telegram响应: {}", response);

            JSONObject resObj = JSON.parseObject(response);
            if (resObj != null && resObj.getBooleanValue("ok")) {
                msgDetail.setThirdPartyMsgId(resObj.getJSONObject("result").getString("message_id"));
                return true;
            } else {
                String error = resObj != null ? resObj.getString("description") : "Unknown error";
                msgDetail.setErrorMsg(error);
                return false;
            }

        } catch (Exception e) {
            log.error("Telegram发送异常", e);
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
