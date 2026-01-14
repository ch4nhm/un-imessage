package com.unimessage.mq.producer;

import com.alibaba.fastjson2.JSON;
import com.unimessage.cache.CacheService;
import com.unimessage.constant.CacheKeyConstants;
import com.unimessage.dto.MqMessage;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Redis MQ 生产者
 *
 * @author 海明
 */
@Component
@ConditionalOnProperty(name = "un-imessage.mq.type", havingValue = "redis", matchIfMissing = true)
public class RedisMqProducer implements MqProducer {

    @Resource
    private CacheService cacheService;

    @Override
    public void send(MqMessage message) {
        cacheService.lPush(CacheKeyConstants.MQ_SEND_QUEUE, JSON.toJSONString(message));
    }
}
