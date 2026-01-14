package com.unimessage.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 发送请求
 *
 * @author 海明
 * @since 2025-12-04
 */
@Data
public class SendRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板编码 (必填)
     */
    private String templateCode;

    /**
     * 接收者列表 (可选)
     * 如果为空，将使用模板关联的接收人分组
     */
    private List<String> recipients;

    /**
     * 模板参数 (可选)
     */
    private Map<String, Object> params;

    /**
     * 业务唯一ID (可选, 用于幂等去重)
     */
    private String bizId;
}
