package com.unimessage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统配置实体类
 *
 * @author 海明
 */
@Data
@TableName("sys_config")
public class SysConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 系统Logo (Base64)
     */
    private String logo;

    /**
     * 系统图标 (Base64)
     */
    private String icon;
}
