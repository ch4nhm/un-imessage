package com.unimessage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unimessage.dto.ChartDataDto;
import com.unimessage.entity.LogMsgDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 消息详情表 Mapper 接口
 * </p>
 *
 * @author 海明
 * @since 2025-12-04
 */
@Mapper
public interface LogMsgDetailMapper extends BaseMapper<LogMsgDetail> {

    /**
     * 获取近7天发送趋势
     *
     * @return 每日发送量统计列表
     */
    @Select("SELECT DATE_FORMAT(created_at, '%Y-%m-%d') as name, COUNT(*) as value FROM log_msg_detail WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) GROUP BY name ORDER BY name")
    List<ChartDataDto> getDailyTrend();

    /**
     * 获取发送状态分布
     *
     * @return 状态分布统计列表
     */
    @Select("SELECT CAST(status AS CHAR) as name, COUNT(*) as value FROM log_msg_detail GROUP BY status")
    List<ChartDataDto> getStatusDist();

    /**
     * 获取最近30天发送状态分布
     *
     * @return 状态分布统计列表
     */
    @Select("SELECT CAST(status AS CHAR) as name, COUNT(*) as value FROM log_msg_detail WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) GROUP BY status")
    List<ChartDataDto> getStatusDistRecent();
}
