package com.unimessage.sdk.autoconfigure;

import com.unimessage.sdk.client.impl.UniMessageClientImpl;
import com.unimessage.sdk.client.UniMessageClient;
import com.unimessage.sdk.config.UniMessageProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * UniMessage 自动配置类
 *
 * @author 海明
 * @since 2025-12-08
 */
@AutoConfiguration
@EnableConfigurationProperties(UniMessageProperties.class)
@ConditionalOnClass(UniMessageClient.class)
@ConditionalOnProperty(prefix = "un-imessage.client", name = "enabled", havingValue = "true", matchIfMissing = true)
public class UniMessageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public UniMessageClient uniMessageClient(UniMessageProperties properties) {
        return new UniMessageClientImpl(properties);
    }
}
