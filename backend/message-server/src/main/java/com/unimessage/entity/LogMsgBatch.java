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
 * 消息发送批次记录表
 *
 * @author 海明
 * @since 2025-12-04
 */
@Data
@TableName("log_msg_batch")
public class LogMsgBatch implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 业务批次号 (UUID)
     */
    private String batchNo;

    /**
     * 调用方ID
     */
    private Long appId;

    /**
     * 使用的模板ID
     */
    private Long templateId;

    /**
     * 模板名称快照
     */
    private String templateName;

    /**
     * 实际发送渠道ID
     */
    private Long channelId;

    /**
     * 渠道名称快照
     */
    private String channelName;

    /**
     * 冗余消息类型
     */
    private Integer msgType;

    /**
     * 最终发送标题
     */
    private String title;

    /**
     * 模板内容快照
     */
    private String content;

    /**
     * 业务方传入的参数JSON
     */
    private String contentParams;

    /**
     * 总发送人数
     */
    private Integer totalCount;

    /**
     * 成功人数
     */
    private Integer successCount;

    /**
     * 失败人数
     */
    private Integer failCount;

    /**
     * 批次状态: 0处理中 10全部成功 20部分成功 30全部失败
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
