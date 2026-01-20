package com.unimessage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unimessage.entity.ShortUrl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 短链接 Mapper
 *
 * @author 海明
 * @since 2026-01-14
 */
@Mapper
public interface ShortUrlMapper extends BaseMapper<ShortUrl> {

    /**
     * 增加点击次数
     *
     * @param shortCode 短链码
     * @return 影响行数
     */
    @Update("UPDATE short_url SET click_count = click_count + 1 WHERE short_code = #{shortCode}")
    int incrementClickCount(@Param("shortCode") String shortCode);
}
