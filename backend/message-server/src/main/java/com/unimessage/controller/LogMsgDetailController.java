package com.unimessage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unimessage.common.Result;
import com.unimessage.entity.LogMsgDetail;
import com.unimessage.mapper.LogMsgDetailMapper;
import com.unimessage.service.MessageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息详情查询控制器
 *
 * @author 海明
 * @since 2025-12-04
 */
@RestController
@RequestMapping("/api/v1/log/detail")
public class LogMsgDetailController {

    @Resource
    private LogMsgDetailMapper detailMapper;
    @Resource
    private MessageService messageService;

    /**
     * 分页查询详情列表
     */
    @GetMapping("/page")
    public Result<IPage<LogMsgDetail>> page(@RequestParam(defaultValue = "1") Integer current,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) Long batchId,
                                            @RequestParam(required = false) String recipient,
                                            @RequestParam(required = false) Integer status) {
        Page<LogMsgDetail> page = new Page<>(current, size);
        LambdaQueryWrapper<LogMsgDetail> wrapper = new LambdaQueryWrapper<>();

        if (batchId != null) {
            wrapper.eq(LogMsgDetail::getBatchId, batchId);
        }
        if (recipient != null && !recipient.isEmpty()) {
            wrapper.like(LogMsgDetail::getRecipient, recipient);
        }
        if (status != null) {
            wrapper.eq(LogMsgDetail::getStatus, status);
        }

        wrapper.orderByDesc(LogMsgDetail::getCreatedAt);
        return Result.success(detailMapper.selectPage(page, wrapper));
    }

    /**
     * 根据批次ID分页查询详情
     */
    @GetMapping("/batch/{batchId}")
    public Result<IPage<LogMsgDetail>> getByBatchId(@PathVariable Long batchId,
                                                    @RequestParam(defaultValue = "1") Integer current,
                                                    @RequestParam(defaultValue = "10") Integer size) {
        Page<LogMsgDetail> page = new Page<>(current, size);
        LambdaQueryWrapper<LogMsgDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LogMsgDetail::getBatchId, batchId);
        wrapper.orderByDesc(LogMsgDetail::getCreatedAt);
        return Result.success(detailMapper.selectPage(page, wrapper));
    }

    /**
     * 根据ID查询详情
     */
    @GetMapping("/{id}")
    public Result<LogMsgDetail> getById(@PathVariable Long id) {
        return Result.success(detailMapper.selectById(id));
    }

    /**
     * 查询失败的消息
     */
    @GetMapping("/failed")
    public Result<IPage<LogMsgDetail>> getFailedMessages(@RequestParam(defaultValue = "1") Integer current,
                                                         @RequestParam(defaultValue = "10") Integer size,
                                                         @RequestParam(required = false) String startTime,
                                                         @RequestParam(required = false) String endTime) {
        Page<LogMsgDetail> page = new Page<>(current, size);
        LambdaQueryWrapper<LogMsgDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LogMsgDetail::getStatus, 30);

        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(LogMsgDetail::getCreatedAt, LocalDateTime.parse(startTime));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(LogMsgDetail::getCreatedAt, LocalDateTime.parse(endTime));
        }

        wrapper.orderByDesc(LogMsgDetail::getCreatedAt);
        return Result.success(detailMapper.selectPage(page, wrapper));
    }

    /**
     * 重试发送
     */
    @PostMapping("/{id}/retry")
    public Result<Boolean> retry(@PathVariable Long id) {
        return Result.success(messageService.retry(id));
    }
}
