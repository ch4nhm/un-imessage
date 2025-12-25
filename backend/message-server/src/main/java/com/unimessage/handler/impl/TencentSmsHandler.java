package com.unimessage.handler.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import com.unimessage.entity.LogMsgDetail;
import com.unimessage.entity.SysChannel;
import com.unimessage.entity.SysTemplate;
import com.unimessage.enums.ChannelType;
import com.unimessage.handler.ChannelHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 腾讯云短信发送处理器
 *
 * @author 海明
 * @since 2025-12-25
 */
@Slf4j
@Component
public class TencentSmsHandler implements ChannelHandler {

    @Override
    public boolean support(String channelType) {
        return ChannelType.TENCENT_SMS.getCode().equals(channelType);
    }

    @Override
    public boolean send(SysChannel channel, SysTemplate template, LogMsgDetail msgDetail, Map<String, Object> params) {
        log.info("开始发送腾讯云短信: recipient={}", msgDetail.getRecipient());

        try {
            JSONObject config = JSON.parseObject(channel.getConfigJson());
            String secretId = config.getString("secretId");
            String secretKey = config.getString("secretKey");
            String sdkAppId = config.getString("sdkAppId");
            String signName = config.getString("signName");
            String region = config.getString("region"); // e.g., "ap-guangzhou"

            if (secretId == null || secretKey == null || sdkAppId == null || signName == null) {
                throw new IllegalArgumentException("腾讯云短信配置缺失");
            }
            if (region == null || region.isEmpty()) {
                region = "ap-guangzhou";
            }

            Credential cred = new Credential(secretId, secretKey);
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");

            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            SmsClient client = new SmsClient(cred, region, clientProfile);

            SendSmsRequest req = new SendSmsRequest();
            req.setSmsSdkAppId(sdkAppId);
            req.setSignName(signName);
            req.setTemplateId(template.getThirdPartyId());

            String[] phoneNumberSet = {msgDetail.getRecipient()};
            req.setPhoneNumberSet(phoneNumberSet);

            List<String> paramList = new ArrayList<>();
            if (params != null) {
                // Try to find "1", "2"...
                for (int i = 1; i <= params.size(); i++) {
                    if (params.containsKey(String.valueOf(i))) {
                        paramList.add(String.valueOf(params.get(String.valueOf(i))));
                    } else {
                        break;
                    }
                }
                if (paramList.isEmpty() && !params.isEmpty()) {
                    for (Object val : params.values()) {
                        paramList.add(String.valueOf(val));
                    }
                }
            }
            req.setTemplateParamSet(paramList.toArray(new String[0]));

            // Snapshot
            msgDetail.setContent("TemplateId: " + template.getThirdPartyId() + ", Params: " + JSON.toJSONString(paramList));

            SendSmsResponse resp = client.SendSms(req);

            SendStatus[] sendStatusSet = resp.getSendStatusSet();
            if (sendStatusSet != null && sendStatusSet.length > 0) {
                SendStatus status = sendStatusSet[0];
                if ("Ok".equalsIgnoreCase(status.getCode())) {
                    msgDetail.setThirdPartyMsgId(status.getSerialNo());
                    return true;
                } else {
                    msgDetail.setErrorMsg(status.getMessage());
                    return false;
                }
            }
            return false;

        } catch (Exception e) {
            log.error("腾讯云短信发送异常", e);
            msgDetail.setErrorMsg(e.getMessage());
            return false;
        }
    }
}
