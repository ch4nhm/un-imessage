package com.unimessage.enums;

import lombok.Getter;

/**
 * 渠道类型枚举
 *
 * @author 海明
 * @since 2025-12-04
 */
@Getter
public enum ChannelType {

    /**
     * 短信
     */
    SMS("SMS", "短信"),

    /**
     * 邮件
     */
    EMAIL("EMAIL", "邮件"),

    /**
     * 微信服务号
     */
    WECHAT_OFFICIAL("WECHAT_OFFICIAL", "微信服务号"),

    /**
     * 企业微信
     */
    WECHAT_WORK("WECHAT_WORK", "企业微信"),

    /**
     * 钉钉
     */
    DINGTALK("DINGTALK", "钉钉"),

    /**
     * 飞书
     */
    FEISHU("FEISHU", "飞书"),

    /**
     * Telegram
     */
    TELEGRAM("TELEGRAM", "Telegram"),

    /**
     * Slack
     */
    SLACK("SLACK", "Slack"),

    /**
     * 腾讯云短信
     */
    TENCENT_SMS("TENCENT_SMS", "腾讯云短信"),

    /**
     * Twilio 短信
     */
    TWILIO("TWILIO", "Twilio"),

    /**
     * Webhook
     */
    WEBHOOK("WEBHOOK", "Webhook");

    /**
     * 渠道类型代码
     */
    private final String code;

    /**
     * 渠道类型名称
     */
    private final String name;

    ChannelType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 渠道类型代码
     * @return 对应的枚举，如果不存在则返回 null
     */
    public static ChannelType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ChannelType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断代码是否有效
     *
     * @param code 渠道类型代码
     * @return true 如果代码有效
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}
