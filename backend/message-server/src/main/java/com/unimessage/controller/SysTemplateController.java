package com.unimessage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unimessage.common.Result;
import com.unimessage.dto.SysTemplateDto;
import com.unimessage.entity.SysTemplate;
import com.unimessage.mapper.SysTemplateMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息模板管理控制器
 *
 * @author 海明
 * @since 2025-12-04
 */
@RestController
@RequestMapping("/api/v1/template")
public class SysTemplateController {

    @Resource
    private SysTemplateMapper templateMapper;

    /**
     * 分页查询模板列表
     */
    @GetMapping("/page")
    public Result<IPage<SysTemplate>> page(@RequestParam(defaultValue = "1") Integer current,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestParam(required = false) Long appId,
                                           @RequestParam(required = false) Long channelId,
                                           @RequestParam(required = false) String name) {
        Page<SysTemplate> page = new Page<>(current, size);
        LambdaQueryWrapper<SysTemplate> wrapper = new LambdaQueryWrapper<>();
        if (appId != null) {
            wrapper.eq(SysTemplate::getAppId, appId);
        }
        if (channelId != null) {
            wrapper.eq(SysTemplate::getChannelId, channelId);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like(SysTemplate::getName, name);
        }
        wrapper.orderByDesc(SysTemplate::getCreatedAt);
        return Result.success(templateMapper.selectPage(page, wrapper));
    }

    /**
     * 查询所有可用模板
     */
    @GetMapping("/list")
    public Result<List<SysTemplate>> list(@RequestParam(required = false) Long appId,
                                          @RequestParam(required = false) Long channelId) {
        LambdaQueryWrapper<SysTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTemplate::getStatus, 1);
        if (appId != null) {
            wrapper.eq(SysTemplate::getAppId, appId);
        }
        if (channelId != null) {
            wrapper.eq(SysTemplate::getChannelId, channelId);
        }
        wrapper.orderByDesc(SysTemplate::getCreatedAt);
        return Result.success(templateMapper.selectList(wrapper));
    }

    /**
     * 根据ID查询模板
     */
    @GetMapping("/{id}")
    public Result<SysTemplate> getById(@PathVariable Long id) {
        return Result.success(templateMapper.selectById(id));
    }

    /**
     * 根据模板代码查询
     */
    @GetMapping("/code/{code}")
    public Result<SysTemplate> getByCode(@PathVariable String code) {
        LambdaQueryWrapper<SysTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTemplate::getCode, code);
        return Result.success(templateMapper.selectOne(wrapper));
    }

    /**
     * 创建模板
     */
    @PostMapping
    public Result<SysTemplate> create(@RequestBody SysTemplateDto templateDto) {
        SysTemplate template = new SysTemplate();
        BeanUtils.copyProperties(templateDto, template);

        // Handle List -> String conversion
        if (templateDto.getRecipientGroupIds() != null && !templateDto.getRecipientGroupIds().isEmpty()) {
            template.setRecipientGroupIds(String.join(",", templateDto.getRecipientGroupIds().stream().map(String::valueOf).toArray(String[]::new)));
        }
        if (templateDto.getRecipientIds() != null && !templateDto.getRecipientIds().isEmpty()) {
            template.setRecipientIds(String.join(",", templateDto.getRecipientIds().stream().map(String::valueOf).toArray(String[]::new)));
        }

        template.setCreatedAt(LocalDateTime.now());
        if (template.getStatus() == null) {
            template.setStatus(1);
        }
        templateMapper.insert(template);
        return Result.success(template);
    }

    /**
     * 更新模板
     */
    @PutMapping("/{id}")
    public Result<SysTemplate> update(@PathVariable Long id, @RequestBody SysTemplateDto templateDto) {
        SysTemplate template = new SysTemplate();
        BeanUtils.copyProperties(templateDto, template);

        // Handle List -> String conversion
        if (templateDto.getRecipientGroupIds() != null) {
            template.setRecipientGroupIds(String.join(",", templateDto.getRecipientGroupIds().stream().map(String::valueOf).toArray(String[]::new)));
        } else {
            // Clear if null
            template.setRecipientGroupIds("");
        }

        if (templateDto.getRecipientIds() != null) {
            template.setRecipientIds(String.join(",", templateDto.getRecipientIds().stream().map(String::valueOf).toArray(String[]::new)));
        } else {
            // Clear if null
            template.setRecipientIds("");
        }

        template.setId(id);
        templateMapper.updateById(template);
        return Result.success(templateMapper.selectById(id));
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        templateMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 启用/禁用模板
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysTemplate template = new SysTemplate();
        template.setId(id);
        template.setStatus(status);
        templateMapper.updateById(template);
        return Result.success();
    }
}
