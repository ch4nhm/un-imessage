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
 * 消息模板表
 *
 * @author 海明
 * @since 2025-12-04
 */
@Data
@TableName("sys_template")
public class SysTemplate implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板编码 (业务方SDK调用凭证)
     */
    private String code;

    /**
     * 关联的应用ID (null表示公共模板)
     */
    private Long appId;

    /**
     * 关联的渠道ID
     */
    private Long channelId;

    /**
     * 消息类型: 10通知 20营销 30验证码
     */
    private Integer msgType;

    /**
     * 第三方模板ID (如阿里云短信模板Code)
     */
    private String thirdPartyId;

    /**
     * 消息标题 (邮件/钉钉等需要)
     */
    private String title;

    /**
     * 消息内容模板 (支持占位符 ${code})
     */
    private String content;

    /**
     * 预期变量列表 (JSON数组, 用于校验)
     */
    private String variables;

    /**
     * 去重配置JSON (可选)
     */
    private String deduplicationConfig;

    /**
     * 关联的接收者分组ID列表 (逗号分隔)
     */
    private String recipientGroupIds;

    /**
     * 关联的接收者ID列表 (逗号分隔)
     */
    private String recipientIds;

    /**
     * 频率限制 (每秒最大请求数, 0或null表示不限制)
     */
    private Integer rateLimit;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
