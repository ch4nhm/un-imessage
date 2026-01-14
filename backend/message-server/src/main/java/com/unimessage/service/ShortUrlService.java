package com.unimessage.service;

import com.unimessage.dto.ShortUrlCreateRequest;
import com.unimessage.dto.ShortUrlResponse;
import com.unimessage.dto.ShortUrlStatsResponse;
import com.unimessage.entity.ShortUrl;

/**
 * 短链接服务接口
 *
 * @author 海明
 * @since 2026-01-14
 */
public interface ShortUrlService {

    /**
     * 创建短链接
     *
     * @param request   创建请求
     * @param createdBy 创建者ID
     * @return 短链接响应
     */
    ShortUrlResponse createShortUrl(ShortUrlCreateRequest request, Long createdBy);

    /**
     * 根据短链码获取原始URL
     *
     * @param shortCode 短链码
     * @return 原始URL, 不存在或已过期返回null
     */
    String getOriginalUrl(String shortCode);

    /**
     * 根据短链码获取短链接实体
     *
     * @param shortCode 短链码
     * @return 短链接实体
     */
    ShortUrl getByShortCode(String shortCode);

    /**
     * 记录访问日志并增加点击量
     *
     * @param shortCode 短链码
     * @param ip        访问者IP
     * @param userAgent User-Agent
     * @param referer   来源页面
     */
    void recordAccess(String shortCode, String ip, String userAgent, String referer);

    /**
     * 获取短链接统计信息
     *
     * @param shortCode 短链码
     * @return 统计信息
     */
    ShortUrlStatsResponse getStats(String shortCode);

    /**
     * 禁用短链接
     *
     * @param shortCode 短链码
     * @return 是否成功
     */
    boolean disableShortUrl(String shortCode);

    /**
     * 删除短链接
     *
     * @param shortCode 短链码
     * @return 是否成功
     */
    boolean deleteShortUrl(String shortCode);
}
