package com.unimessage.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息批次响应 DTO
 *
 * @author 海明
 */
@Data
public class LogMsgBatchRespDto implements Serializable {
    private Long id;
    private String batchNo;
    private Long appId;
    private String appName;
    private Long templateId;
    private String templateName;
    private Long channelId;
    private String channelName;
    private String channelType;
    private Integer msgType;
    private String title;
    private String content;
    private String contentParams;
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
