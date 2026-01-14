package com.unimessage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unimessage.entity.ShortUrlAccessLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短链接访问日志 Mapper
 *
 * @author 海明
 * @since 2026-01-14
 */
@Mapper
public interface ShortUrlAccessLogMapper extends BaseMapper<ShortUrlAccessLog> {
}
