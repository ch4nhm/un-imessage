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
 * 渠道配置表
 *
 * @author 海明
 * @since 2025-12-04
 */
@Data
@TableName("sys_channel")
public class SysChannel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 渠道名称 (如: 阿里云短信-营销)
     */
    private String name;

    /**
     * 渠道类型: SMS, EMAIL, WECHAT_OFFICIAL, WECHAT_WORK, DINGTALK
     */
    private String type;

    /**
     * 供应商: ALIYUN, TENCENT, LOCAL_SMTP
     */
    private String provider;

    /**
     * 账号配置JSON (AccessKey, Secret, Host, Port等)
     */
    private String configJson;

    /**
     * 状态: 1启用 0禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
