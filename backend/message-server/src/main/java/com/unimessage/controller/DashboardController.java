package com.unimessage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unimessage.common.Result;
import com.unimessage.dto.ChartDataDto;
import com.unimessage.dto.DashboardStatsDto;
import com.unimessage.entity.LogMsgDetail;
import com.unimessage.enums.DetailStatus;
import com.unimessage.mapper.LogMsgBatchMapper;
import com.unimessage.mapper.LogMsgDetailMapper;
import com.unimessage.mapper.SysAppMapper;
import com.unimessage.mapper.SysUserMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 仪表盘统计控制器
 *
 * @author 海明
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    @Resource
    private SysAppMapper appMapper;

    @Resource
    private SysUserMapper userMapper;

    @Resource
    private LogMsgDetailMapper detailMapper;

    @Resource
    private LogMsgBatchMapper batchMapper;

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public Result<DashboardStatsDto> getStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        // 1. 应用总数
        stats.setAppCount(appMapper.selectCount(null));

        // 2. 消息总发送量 (限制最近30天)
        LambdaQueryWrapper<LogMsgDetail> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.ge(LogMsgDetail::getCreatedAt, java.time.LocalDateTime.now().minusDays(30));
        Long msgCount = detailMapper.selectCount(countWrapper);
        stats.setMsgCount(msgCount);

        // 3. 用户总数
        stats.setUserCount(userMapper.selectCount(null));

        // 4. 计算成功率 (总成功数 / 总发送数) - 限制最近30天
        if (msgCount > 0) {
            LambdaQueryWrapper<LogMsgDetail> successWrapper = new LambdaQueryWrapper<>();
            successWrapper.eq(LogMsgDetail::getStatus, DetailStatus.SUCCESS.getCode())
                         .ge(LogMsgDetail::getCreatedAt, java.time.LocalDateTime.now().minusDays(30));
            Long successCount = detailMapper.selectCount(successWrapper);

            double rate = (double) successCount / msgCount * 100;
            // 保留一位小数
            BigDecimal bg = new BigDecimal(rate).setScale(1, RoundingMode.HALF_UP);
            stats.setSuccessRate(bg.doubleValue());
        } else {
            stats.setSuccessRate(0.0);
        }

        // 5. 每日发送趋势 (已经限制了7天)
        stats.setTrend(detailMapper.getDailyTrend());

        // 6. 渠道分布
        stats.setChannelDist(batchMapper.getChannelDist());

        // 7. 状态分布 (需转换状态码为中文) - 限制最近30天
        List<ChartDataDto> statusRaw = detailMapper.getStatusDistRecent();
        if (statusRaw != null) {
            List<ChartDataDto> statusDist = statusRaw.stream().peek(item -> {
                try {
                    int code = Integer.parseInt(item.getName());
                    DetailStatus s = DetailStatus.fromCode(code);
                    if (s != null) {
                        item.setName(s.getDesc());
                    }
                } catch (NumberFormatException ignored) {
                }
            }).collect(Collectors.toList());
            stats.setStatusDist(statusDist);
        }

        return Result.success(stats);
    }
}
