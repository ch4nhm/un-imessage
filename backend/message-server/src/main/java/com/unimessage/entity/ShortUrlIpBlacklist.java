package com.unimessage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IP黑名单表
 *
 * @author 海明
 * @since 2026-01-14
 */
@Data
@TableName("short_url_ip_blacklist")
public class ShortUrlIpBlacklist implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 封禁原因
     */
    private String reason;

    /**
     * 解封时间 (NULL表示永久封禁)
     */
    private LocalDateTime expireAt;

    private LocalDateTime createdAt;
}
