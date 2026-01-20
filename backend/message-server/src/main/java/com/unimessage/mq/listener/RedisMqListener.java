package com.unimessage.mq.listener;

import com.alibaba.fastjson2.JSON;
import com.unimessage.cache.CacheService;
import com.unimessage.constant.CacheKeyConstants;
import com.unimessage.dto.MqMessage;
import com.unimessage.service.MessageService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * Redis MQ 监听器
 *
 * @author 海明
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "un-imessage.mq.type", havingValue = "redis", matchIfMissing = true)
public class RedisMqListener {

    private static final String THREAD_NAME_PREFIX_WORKER = "mq-worker-";
    private static final String THREAD_NAME_POLLER = "mq-poller";
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = 20;
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final int QUEUE_CAPACITY = 1000;
    private static final long POP_TIMEOUT = 5;
    private static final long SHUTDOWN_TIMEOUT = 60;

    @Resource
    private CacheService cacheService;
    @Resource
    private MessageService messageService;
    /**
     * 用于执行具体业务逻辑的线程池
     */
    private ThreadPoolExecutor workerExecutor;

    /**
     * 用于运行监听循环的单线程池
     */
    private ExecutorService pollerExecutor;

    private volatile boolean running = true;

    @PostConstruct
    public void init() {
        // 1. 初始化业务处理线程池
        // 核心线程数10，最大线程数20，空闲线程存活时间60s，队列容量1000
        workerExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                r -> {
                    Thread t = new Thread(r);
                    t.setName(THREAD_NAME_PREFIX_WORKER + t.getId());
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        // 2. 初始化监听器线程池 (单线程)
        pollerExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, THREAD_NAME_POLLER));

        // 3. 启动监听
        pollerExecutor.execute(this::listen);

        log.info("Redis MQ Listener started.");
    }

    private void listen() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // 阻塞式弹出，超时时间 5秒
                String messageJson = cacheService.rPop(CacheKeyConstants.MQ_SEND_QUEUE, POP_TIMEOUT, TimeUnit.SECONDS);
                if (messageJson != null) {
                    // 提交给业务线程池处理
                    workerExecutor.submit(() -> processMessage(messageJson));
                }
            } catch (Exception e) {
                if (running) {
                    log.error("Error in Redis MQ Listener loop", e);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    private void processMessage(String messageJson) {
        try {
            MqMessage message = JSON.parseObject(messageJson, MqMessage.class);
            messageService.processBatch(message);
        } catch (Exception e) {
            log.error("Error processing message: {}", messageJson, e);
        }
    }

    @PreDestroy
    public void destroy() {
        running = false;

        // 关闭监听器
        if (pollerExecutor != null) {
            pollerExecutor.shutdownNow();
        }

        // 关闭业务线程池
        if (workerExecutor != null) {
            workerExecutor.shutdown();
            try {
                if (!workerExecutor.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                    workerExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                workerExecutor.shutdownNow();
            }
        }
        log.info("Redis MQ Listener stopped.");
    }
}
