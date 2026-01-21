package com.unimessage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unimessage.common.Result;
import com.unimessage.dto.SysRecipientDto;
import com.unimessage.entity.SysRecipient;
import com.unimessage.mapper.SysRecipientMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 接收者管理控制器
 *
 * @author 海明
 * @since 2025-12-11
 */
@RestController
@RequestMapping("/api/v1/recipient")
public class SysRecipientController {

    @Resource
    private SysRecipientMapper recipientMapper;

    /**
     * 分页查询接收者列表
     */
    @GetMapping("/page")
    public Result<IPage<SysRecipient>> page(@RequestParam(defaultValue = "1") Integer current,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) String name,
                                            @RequestParam(required = false) String mobile) {
        Page<SysRecipient> page = new Page<>(current, size);
        LambdaQueryWrapper<SysRecipient> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(SysRecipient::getName, name);
        }
        if (mobile != null && !mobile.isEmpty()) {
            wrapper.like(SysRecipient::getMobile, mobile);
        }
        wrapper.orderByDesc(SysRecipient::getCreatedAt);
        IPage<SysRecipient> resultPage = recipientMapper.selectPage(page, wrapper);
        return Result.success(resultPage);
    }

    /**
     * 查询所有接收者
     */
    @GetMapping("/list")
    public Result<List<SysRecipient>> list() {
        LambdaQueryWrapper<SysRecipient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRecipient::getStatus, 1);
        wrapper.orderByDesc(SysRecipient::getCreatedAt);
        return Result.success(recipientMapper.selectList(wrapper));
    }

    /**
     * 根据ID查询接收者
     */
    @GetMapping("/{id}")
    public Result<SysRecipient> getById(@PathVariable Long id) {
        return Result.success(recipientMapper.selectById(id));
    }

    /**
     * 创建接收者
     */
    @PostMapping
    public Result<SysRecipient> create(@RequestBody SysRecipientDto recipientDto) {
        SysRecipient recipient = new SysRecipient();
        BeanUtils.copyProperties(recipientDto, recipient);

        recipient.setCreatedAt(LocalDateTime.now());
        recipient.setUpdatedAt(LocalDateTime.now());
        if (recipient.getStatus() == null) {
            recipient.setStatus(1);
        }
        recipientMapper.insert(recipient);
        return Result.success(recipient);
    }

    /**
     * 更新接收者
     */
    @PutMapping("/{id}")
    public Result<SysRecipient> update(@PathVariable Long id, @RequestBody SysRecipientDto recipientDto) {
        SysRecipient recipient = recipientMapper.selectById(id);
        if (recipient == null) {
            return Result.fail("接收者不存在");
        }

        BeanUtils.copyProperties(recipientDto, recipient);
        recipient.setId(id);
        recipient.setUpdatedAt(LocalDateTime.now());

        recipientMapper.updateById(recipient);
        return Result.success(recipient);
    }

    /**
     * 删除接收者
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        recipientMapper.deleteById(id);
        return Result.success();
    }
}