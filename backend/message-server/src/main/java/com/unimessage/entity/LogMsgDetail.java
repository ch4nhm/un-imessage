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
 * 消息发送详情表
 *
 * @author 海明
 * @since 2025-12-04
 */
@Data
@TableName("log_msg_detail")
public class LogMsgDetail implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联批次ID
     */
    private Long batchId;

    /**
     * 接收者 (手机号/邮箱/OpenID/UserId)
     */
    private String recipient;

    /**
     * 接收者名称 (如果有)
     */
    private String recipientName;

    /**
     * 最终渲染后的内容 (可选，用于审计)
     */
    private String content;

    /**
     * 发送状态: 10发送中 20发送成功 30发送失败
     */
    private Integer status;

    /**
     * 第三方返回的消息ID (用于回执查询)
     */
    private String thirdPartyMsgId;

    /**
     * 失败原因
     */
    private String errorMsg;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 实际发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
