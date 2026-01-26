package com.unimessage.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.unimessage.interceptor.AppAuthInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 *
 * @author 海明
 * @since 2025-12-04
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private AppAuthInterceptor appAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // App 鉴权拦截器 - 针对消息发送接口和短链创建接口
        registry.addInterceptor(appAuthInterceptor)
                .addPathPatterns("/api/v1/message/**")
                // 只对短链创建接口使用 App 鉴权
                .addPathPatterns("/api/v1/short-url")
                // 排除其他短链管理接口
                .excludePathPatterns("/api/v1/short-url/**");

        // Sa-Token 登录拉截器 - 针对管理后台接口
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/api/**")
                // 排除消息发送接口
                .excludePathPatterns("/api/v1/message/**")
                // 排除短链创建接口（使用 App 鉴权）
                .excludePathPatterns("/api/v1/short-url")
                // 排除登录接口
                .excludePathPatterns("/api/v1/auth/login")
                // 排除健康检查
                .excludePathPatterns("/api/health")
                // 排除短链接重定向 (由单独的Controller处理，不在/api路径下)
                .excludePathPatterns("/s/**");
    }
}
