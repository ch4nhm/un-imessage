package com.unimessage.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短链接响应
 *
 * @author 海明
 * @since 2026-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 短链码
     */
    private String shortCode;

    /**
     * 完整短链接
     */
    private String shortUrl;

    /**
     * 原始链接
     */
    private String originalUrl;

    /**
     * 点击次数
     */
    private Long clickCount;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireAt;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 状态: 1启用 0禁用
     */
    private Integer status;
}
