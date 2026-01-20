package com.unimessage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unimessage.entity.ShortUrlIpBlacklist;
import org.apache.ibatis.annotations.Mapper;

/**
 * IP黑名单 Mapper
 *
 * @author 海明
 * @since 2026-01-14
 */
@Mapper
public interface ShortUrlIpBlacklistMapper extends BaseMapper<ShortUrlIpBlacklist> {
}
