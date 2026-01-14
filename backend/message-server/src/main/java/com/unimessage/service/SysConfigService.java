package com.unimessage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.unimessage.entity.SysConfig;

/**
 * 系统配置服务接口
 * <p>
 * 提供系统配置信息的获取和更新功能，继承自通用服务接口IService
 * </p>
 *
 * @author 海明
 */
public interface SysConfigService extends IService<SysConfig> {

    /**
     * 获取系统配置信息
     *
     * @return SysConfig 系统配置对象，包含当前系统的各项配置参数
     */
    SysConfig getConfig();

    /**
     * 更新系统配置信息
     *
     * @param config 需要更新的系统配置对象，包含新的配置参数值
     */
    void updateConfig(SysConfig config);
}
