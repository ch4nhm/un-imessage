package com.unimessage.handler.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.unimessage.entity.LogMsgDetail;
import com.unimessage.entity.SysChannel;
import com.unimessage.entity.SysTemplate;
import com.unimessage.enums.ChannelType;
import com.unimessage.handler.ChannelHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Twilio 短信发送处理器
 *
 * @author 海明
 * @since 2025-12-25
 */
@Slf4j
@Component
public class TwilioHandler implements ChannelHandler {

    @Override
    public boolean support(String channelType) {
        return ChannelType.TWILIO.getCode().equals(channelType);
    }

    @Override
    public boolean send(SysChannel channel, SysTemplate template, LogMsgDetail msgDetail, Map<String, Object> params) {
        log.info("开始发送Twilio短信: recipient={}", msgDetail.getRecipient());

        try {
            JSONObject config = JSON.parseObject(channel.getConfigJson());
            String accountSid = config.getString("accountSid");
            String authToken = config.getString("authToken");
            String fromPhone = config.getString("fromPhone");

            if (accountSid == null || authToken == null || fromPhone == null) {
                throw new IllegalArgumentException("Twilio配置缺失");
            }

            Twilio.init(accountSid, authToken);

            String content = renderContent(template.getContent(), params);
            msgDetail.setContent(content);

            Message message = Message.creator(
                            new PhoneNumber(msgDetail.getRecipient()),
                            new PhoneNumber(fromPhone),
                            content)
                    .create();

            if (message.getSid() != null) {
                msgDetail.setThirdPartyMsgId(message.getSid());
                return true;
            } else {
                msgDetail.setErrorMsg(message.getErrorMessage());
                return false;
            }

        } catch (Exception e) {
            log.error("Twilio发送异常", e);
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
