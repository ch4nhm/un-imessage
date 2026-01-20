package com.unimessage.handler.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.unimessage.entity.LogMsgDetail;
import com.unimessage.entity.SysChannel;
import com.unimessage.entity.SysTemplate;
import com.unimessage.enums.ChannelType;
import com.unimessage.handler.ChannelHandler;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信服务号消息发送处理器
 * 支持模板消息发送，使用客户端缓存提升性能
 *
 * @author 海明
 * @since 2025-12-04
 */
@Slf4j
@Component
public class WechatOfficialHandler implements ChannelHandler {

    private final Map<Long, WxMpService> clientCache = new ConcurrentHashMap<>();

    @Override
    public boolean support(String channelType) {
        return ChannelType.WECHAT_OFFICIAL.getCode().equals(channelType);
    }

    @Override
    public boolean send(SysChannel channel, SysTemplate template, LogMsgDetail msgDetail, Map<String, Object> params) {
        log.info("开始发送微信服务号消息: recipient={}", msgDetail.getRecipient());

        try {
            WxMpService wxMpService = getService(channel);
            String redirectUrlKey = "redirectUrl";
            JSONObject config = JSON.parseObject(channel.getConfigJson());
            String redirectUrl = config.getString(redirectUrlKey);
            if (params.containsKey(redirectUrlKey)) {
                redirectUrl = params.get(redirectUrlKey).toString();
            }

            WxMpTemplateMessage.WxMpTemplateMessageBuilder builder = WxMpTemplateMessage.builder()
                    .toUser(msgDetail.getRecipient())
                    .templateId(template.getThirdPartyId());

            if (redirectUrl != null && !redirectUrl.isEmpty()) {
                builder.url(redirectUrl);
            }

            WxMpTemplateMessage msg = builder.build();
            // 处理变量
            String variables = template.getVariables();
            if (StringUtils.isNotBlank(variables)) {
                List<String> variableList = JSON.parseArray(variables, String.class);
                for (String variable : variableList) {
                    Object value = params.get(variable);
                    if (value != null) {
                        msg.addData(new WxMpTemplateData(variable, String.valueOf(value)));
                    }
                }
            }
            // 兼容旧版变量
            if (StringUtils.isBlank(variables)) {
                if (CollectionUtil.isNotEmpty(params)) {
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        msg.addData(new WxMpTemplateData(entry.getKey(), String.valueOf(entry.getValue())));
                    }
                }
            }

            // 保存发送内容快照
            msgDetail.setContent(JSON.toJSONString(msg.getData()));

            String msgId = wxMpService.getTemplateMsgService().sendTemplateMsg(msg);

            log.info("微信服务号消息发送成功: msgId={}", msgId);
            msgDetail.setThirdPartyMsgId(msgId);
            return true;

        } catch (Exception e) {
            log.error("微信服务号消息发送异常", e);
            msgDetail.setErrorMsg(e.getMessage());
            return false;
        }
    }

    /**
     * 获取或创建 WxMpService 实例（带缓存）
     */
    private WxMpService getService(SysChannel channel) {
        return clientCache.computeIfAbsent(channel.getId(), k -> {
            JSONObject config = JSON.parseObject(channel.getConfigJson());
            String appId = config.getString("appId");
            String secret = config.getString("secret");

            if (appId == null || appId.isEmpty() || secret == null || secret.isEmpty()) {
                throw new IllegalArgumentException("微信服务号配置缺失: appId 或 secret 为空");
            }

            WxMpDefaultConfigImpl wxConfig = new WxMpDefaultConfigImpl();
            wxConfig.setAppId(appId);
            wxConfig.setSecret(secret);
            WxMpService service = new WxMpServiceImpl();
            service.setWxMpConfigStorage(wxConfig);

            log.info("初始化微信服务号 SDK 客户端: channelId={}, appId={}", channel.getId(), appId);
            return service;
        });
    }
}
