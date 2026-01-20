package com.unimessage.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 短链接统计响应
 *
 * @author 海明
 * @since 2026-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlStatsResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 短链码
     */
    private String shortCode;

    /**
     * 原始链接
     */
    private String originalUrl;

    /**
     * 总点击次数
     */
    private Long totalClicks;

    /**
     * 今日点击次数
     */
    private Long todayClicks;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 最近访问记录
     */
    private List<AccessRecord> recentAccess;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccessRecord implements Serializable {
        private String ip;
        private String userAgent;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime accessTime;
    }
}
