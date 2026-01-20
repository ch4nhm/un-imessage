package com.unimessage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 短链接创建请求
 *
 * @author 海明
 * @since 2026-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlCreateRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 原始长链接 (必填)
     */
    private String url;

    /**
     * 自定义短链码 (可选, 4-10位字母数字)
     */
    private String customCode;

    /**
     * 有效期(秒), 0或null表示永不过期
     */
    private Long ttl;
}
