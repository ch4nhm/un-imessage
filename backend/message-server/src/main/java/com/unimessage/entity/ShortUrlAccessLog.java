package com.unimessage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短链接访问日志表
 *
 * @author 海明
 * @since 2026-01-14
 */
@Data
@TableName("short_url_access_log")
public class ShortUrlAccessLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 短链码
     */
    private String shortCode;

    /**
     * 访问者IP
     */
    private String ip;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 来源页面
     */
    private String referer;

    /**
     * 访问时间
     */
    private LocalDateTime accessTime;
}
