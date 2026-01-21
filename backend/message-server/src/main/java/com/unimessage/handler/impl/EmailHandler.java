package com.unimessage.handler.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.unimessage.entity.LogMsgDetail;
import com.unimessage.entity.SysChannel;
import com.unimessage.entity.SysTemplate;
import com.unimessage.enums.ChannelType;
import com.unimessage.handler.ChannelHandler;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;

/**
 * 邮件发送处理器
 * 重构：使用 Spring JavaMailSender 替代 Hutool 以解决 Jakarta/Javax 冲突
 *
 * @author 海明
 * @since 2025-12-04
 */
@Slf4j
@Component
public class EmailHandler implements ChannelHandler {

    @Override
    public boolean support(String channelType) {
        return ChannelType.EMAIL.getCode().equals(channelType);
    }

    @Override
    public boolean send(SysChannel channel, SysTemplate template, LogMsgDetail msgDetail, Map<String, Object> params) {
        log.info("开始发送邮件: recipient={}", msgDetail.getRecipient());

        try {
            // 1. 解析配置
            JSONObject config = JSON.parseObject(channel.getConfigJson());
            String host = config.getString("host");
            Integer port = config.getInteger("port");
            String username = config.getString("username");
            String password = config.getString("password");
            Boolean ssl = config.getBoolean("ssl");

            validateConfig(host, port, username, password);

            // 2. 动态构建 JavaMailSender
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(host);
            mailSender.setPort(port);
            mailSender.setUsername(username);
            mailSender.setPassword(password);
            mailSender.setDefaultEncoding("UTF-8");

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.connectiontimeout", "5000");
            props.put("mail.smtp.timeout", "5000");
            props.put("mail.smtp.writetimeout", "5000");

            if (Boolean.TRUE.equals(ssl)) {
                props.put("mail.smtp.ssl.enable", "true");
                // 某些环境可能需要指定 socketFactory，但在 Spring Boot 3 + Jakarta Mail 中通常只需 ssl.enable
            }

            // 3. 构建邮件内容
            String content = renderContent(template.getContent(), params);
            msgDetail.setContent(content);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(username);
            helper.setTo(msgDetail.getRecipient());
            helper.setSubject(template.getTitle());
            // true 表示支持 HTML
            helper.setText(content, true);

            // 4. 发送
            mailSender.send(mimeMessage);

            // JavaMailSender 不直接返回 Message-ID，通常如果没报错即为成功
            String msgId = mimeMessage.getMessageID();
            if (msgId == null) {
            // Fallback
                msgId = String.valueOf(System.currentTimeMillis());
            }

            log.info("邮件发送成功");
            msgDetail.setThirdPartyMsgId(msgId);
            return true;

        } catch (Exception e) {
            log.error("邮件发送异常", e);
            msgDetail.setErrorMsg(e.getMessage());
            return false;
        }
    }

    /**
     * 校验配置参数
     */
    private void validateConfig(String host, Integer port, String username, String password) {
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("邮件配置缺失: host 为空");
        }
        if (port == null) {
            throw new IllegalArgumentException("邮件配置缺失: port 为空");
        }
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("邮件配置缺失: username 为空");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("邮件配置缺失: password 为空");
        }
    }

    /**
     * 渲染模板内容
     */
    private String renderContent(String template, Map<String, Object> params) {
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
