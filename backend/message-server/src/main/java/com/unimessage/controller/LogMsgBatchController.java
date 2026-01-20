package com.unimessage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unimessage.common.Result;
import com.unimessage.dto.BatchStatisticsDto;
import com.unimessage.dto.LogMsgBatchRespDto;
import com.unimessage.entity.LogMsgBatch;
import com.unimessage.entity.SysApp;
import com.unimessage.mapper.LogMsgBatchMapper;
import com.unimessage.mapper.SysAppMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 消息批次查询控制器
 *
 * @author 海明
 * @since 2025-12-04
 */
@RestController
@RequestMapping("/api/v1/log/batch")
public class LogMsgBatchController {

    @Resource
    private LogMsgBatchMapper batchMapper;

    @Resource
    private SysAppMapper appMapper;

    /**
     * 分页查询批次列表
     */
    @GetMapping("/page")
    public Result<IPage<LogMsgBatchRespDto>> page(@RequestParam(defaultValue = "1") Integer current,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestParam(required = false) Long appId,
                                                  @RequestParam(required = false) Long channelId,
                                                  @RequestParam(required = false) Integer status,
                                                  @RequestParam(required = false) String startTime,
                                                  @RequestParam(required = false) String batchNo,
                                                  @RequestParam(required = false) String endTime) {
        Page<LogMsgBatch> page = new Page<>(current, size);
        LambdaQueryWrapper<LogMsgBatch> wrapper = new LambdaQueryWrapper<>();

        if (appId != null) {
            wrapper.eq(LogMsgBatch::getAppId, appId);
        }
        if (channelId != null) {
            wrapper.eq(LogMsgBatch::getChannelId, channelId);
        }
        if (status != null) {
            wrapper.eq(LogMsgBatch::getStatus, status);
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(LogMsgBatch::getCreatedAt, LocalDateTime.parse(startTime));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(LogMsgBatch::getCreatedAt, LocalDateTime.parse(endTime));
        }
        if (StringUtils.isNotBlank(batchNo)) {
            wrapper.like(LogMsgBatch::getBatchNo, batchNo);
        }

        wrapper.orderByDesc(LogMsgBatch::getCreatedAt);
        Page<LogMsgBatch> batchPage = batchMapper.selectPage(page, wrapper);

        IPage<LogMsgBatchRespDto> resultPage = batchPage.convert(this::convertToDto);
        return Result.success(resultPage);
    }

    /**
     * 根据批次号查询
     */
    @GetMapping("/batchNo/{batchNo}")
    public Result<LogMsgBatchRespDto> getByBatchNo(@PathVariable String batchNo) {
        LambdaQueryWrapper<LogMsgBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LogMsgBatch::getBatchNo, batchNo);
        return Result.success(convertToDto(batchMapper.selectOne(wrapper)));
    }

    /**
     * 根据ID查询批次
     */
    @GetMapping("/{id}")
    public Result<LogMsgBatchRespDto> getById(@PathVariable Long id) {
        return Result.success(convertToDto(batchMapper.selectById(id)));
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public Result<BatchStatisticsDto> getStatistics(@RequestParam(required = false) Long appId,
                                                    @RequestParam(required = false) String startTime,
                                                    @RequestParam(required = false) String endTime) {
        LambdaQueryWrapper<LogMsgBatch> wrapper = new LambdaQueryWrapper<>();

        if (appId != null) {
            wrapper.eq(LogMsgBatch::getAppId, appId);
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(LogMsgBatch::getCreatedAt, LocalDateTime.parse(startTime));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(LogMsgBatch::getCreatedAt, LocalDateTime.parse(endTime));
        }

        Long totalCount = batchMapper.selectCount(wrapper);

        wrapper.eq(LogMsgBatch::getStatus, 10);
        Long successCount = batchMapper.selectCount(wrapper);

        BatchStatisticsDto stats = new BatchStatisticsDto();
        stats.setTotalCount(totalCount);
        stats.setSuccessCount(successCount);
        stats.setFailCount(totalCount - successCount);

        return Result.success(stats);
    }

    private LogMsgBatchRespDto convertToDto(LogMsgBatch batch) {
        if (batch == null) {
            return null;
        }
        LogMsgBatchRespDto dto = new LogMsgBatchRespDto();
        BeanUtils.copyProperties(batch, dto);

        SysApp app = appMapper.selectById(batch.getAppId());
        if (app != null) {
            dto.setAppName(app.getAppName());
        }
        return dto;
    }
}
