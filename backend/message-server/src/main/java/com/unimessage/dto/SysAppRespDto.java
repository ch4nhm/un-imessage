package com.unimessage.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用响应 DTO
 *
 * @author 海明
 */
@Data
public class SysAppRespDto implements Serializable {
    private Long id;
    private String appName;
    private String appCode;
    private String appKey;
    private String appSecret;
    private String owner;
    private String description;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
