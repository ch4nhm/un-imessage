package com.unimessage.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 
 * @author 海明
 * @since 2025-01-20
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 拦截器
     * 主要用于分页功能
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 添加分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        
        // 设置分页参数
        // 单页最大限制数量，默认 500 条，小于 0 如 -1 不受限制
        paginationInterceptor.setMaxLimit(1000L); 
        // 溢出总页数后是否进行处理，默认不处理
        paginationInterceptor.setOverflow(false); 
        // 是否优化 COUNT SQL，默认 true
        paginationInterceptor.setOptimizeJoin(true); 
        
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        return interceptor;
    }
}