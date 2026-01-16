package com.unimessage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短链接映射表
 *
 * @author 海明
 * @since 2026-01-14
 */
@Data
@TableName("short_url")
public class ShortUrl implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 短链码 (Base62编码)
     */
    private String shortCode;

    /**
     * 原始长链接
     */
    private String originalUrl;

    /**
     * 创建者ID
     */
    private Long createdBy;

    /**
     * 点击次数
     */
    private Long clickCount;

    /**
     * 状态: 1启用 0禁用
     */
    private Integer status;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
