package com.unimessage.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户ID工具类
 * 用于处理JSON格式的多渠道用户ID
 *
 * @author 海明
 * @since 2025-01-19
 */
@Slf4j
public class UserIdUtil {

    /**
     * 解析JSON格式的用户ID字符串
     *
     * @param userIdJson JSON格式的用户ID字符串
     * @return 用户ID映射表，key为渠道类型，value为用户ID
     */
    public static Map<String, String> parseUserIds(String userIdJson) {
        if (userIdJson == null || userIdJson.trim().isEmpty()) {
            return new HashMap<>();
        }

        try {
            return JSON.parseObject(userIdJson, Map.class);
        } catch (JSONException e) {
            log.warn("解析用户ID JSON失败: {}", userIdJson, e);
            return new HashMap<>();
        }
    }

    /**
     * 将用户ID映射表转换为JSON字符串
     *
     * @param userIds 用户ID映射表
     * @return JSON格式的用户ID字符串
     */
    public static String toUserIdJson(Map<String, String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return null;
        }

        try {
            return JSON.toJSONString(userIds);
        } catch (Exception e) {
            log.error("转换用户ID为JSON失败", e);
            return null;
        }
    }

    /**
     * 获取指定渠道类型的用户ID
     *
     * @param userIdJson  JSON格式的用户ID字符串
     * @param channelType 渠道类型
     * @return 用户ID，如果不存在则返回null
     */
    public static String getUserId(String userIdJson, String channelType) {
        if (userIdJson == null || userIdJson.trim().isEmpty()) {
            return null;
        }

        try {
            Map<String, String> userIds = parseUserIds(userIdJson);
            return userIds.get(channelType);
        } catch (Exception e) {
            log.warn("解析用户ID JSON失败，尝试作为单一用户ID处理: userIdJson={}, channelType={}", userIdJson, channelType, e);
            // 如果解析失败，可能是旧格式的单一用户ID，直接返回
            return userIdJson;
        }
    }

    /**
     * 添加或更新指定渠道类型的用户ID
     *
     * @param userIdJson  原JSON格式的用户ID字符串
     * @param channelType 渠道类型
     * @param userId      用户ID
     * @return 更新后的JSON格式用户ID字符串
     */
    public static String addUserId(String userIdJson, String channelType, String userId) {
        Map<String, String> userIds = parseUserIds(userIdJson);
        userIds.put(channelType, userId);
        return toUserIdJson(userIds);
    }

    /**
     * 移除指定渠道类型的用户ID
     *
     * @param userIdJson  原JSON格式的用户ID字符串
     * @param channelType 渠道类型
     * @return 更新后的JSON格式用户ID字符串
     */
    public static String removeUserId(String userIdJson, String channelType) {
        Map<String, String> userIds = parseUserIds(userIdJson);
        userIds.remove(channelType);
        return toUserIdJson(userIds);
    }
}