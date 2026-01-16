package com.unimessage.util;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 * 封装 Redis 基础操作，提供统一的访问接口
 *
 * @author Trae
 * @since 2026-01-14
 */
@Slf4j
@Component
public class RedisUtil {

    /**
     * 默认重试次数
     */
    private static final int DEFAULT_RETRY_TIMES = 3;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 写入缓存
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, String value) {
        try {
            stringRedisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis set error: key={}", key, e);
            return false;
        }
    }

    /**
     * 写入缓存并设置过期时间
     *
     * @param key      键
     * @param value    值
     * @param time     时间
     * @param timeUnit 单位
     * @return true成功 false失败
     */
    public boolean set(String key, String value, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                stringRedisTemplate.opsForValue().set(key, value, time, timeUnit);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis set with expire error: key={}", key, e);
            return false;
        }
    }

    /**
     * 读取缓存
     *
     * @param key 键
     * @return 值
     */
    public String get(String key) {
        try {
            return key == null ? null : stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis get error: key={}", key, e);
            return null;
        }
    }

    /**
     * 读取对象（反序列化）
     *
     * @param key   键
     * @param clazz 对象类型
     * @param <T>   泛型
     * @return 对象
     */
    public <T> T getObject(String key, Class<T> clazz) {
        String value = get(key);
        return value == null ? null : JSON.parseObject(value, clazz);
    }

    /**
     * 写入对象（序列化）
     *
     * @param key   键
     * @param value 对象
     * @return true成功 false失败
     */
    public boolean setObject(String key, Object value) {
        return set(key, JSON.toJSONString(value));
    }

    /**
     * 写入对象并设置过期时间
     *
     * @param key      键
     * @param value    对象
     * @param time     时间
     * @param timeUnit 单位
     * @return true成功 false失败
     */
    public boolean setObject(String key, Object value, long time, TimeUnit timeUnit) {
        return set(key, JSON.toJSONString(value), time, timeUnit);
    }

    /**
     * 删除缓存
     *
     * @param key 键
     */
    public void del(String key) {
        try {
            if (key != null) {
                stringRedisTemplate.delete(key);
            }
        } catch (Exception e) {
            log.error("Redis del error: key={}", key, e);
        }
    }

    /**
     * 批量删除缓存
     *
     * @param keys 键集合
     */
    public void del(Collection<String> keys) {
        try {
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("Redis batch del error", e);
        }
    }

    /**
     * 指定缓存失效时间
     *
     * @param key      键
     * @param time     时间
     * @param timeUnit 单位
     * @return true成功 false失败
     */
    public boolean expire(String key, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                stringRedisTemplate.expire(key, time, timeUnit);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis expire error: key={}", key, e);
            return false;
        }
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis hasKey error: key={}", key, e);
            return false;
        }
    }

    /**
     * 如果不存在则设置（原子操作，用于分布式锁/幂等性校验）
     *
     * @param key     键
     * @param value   值
     * @param seconds 过期时间（秒）
     * @return true 设置成功（键不存在），false 键已存在
     */
    public boolean setIfAbsent(String key, String value, long seconds) {
        try {
            Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, value, seconds, TimeUnit.SECONDS);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Redis setIfAbsent error: key={}", key, e);
            return false;
        }
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     * @return 增加后的值
     */
    public Long incr(String key, long delta) {
        try {
            if (delta < 0) {
                throw new RuntimeException("递增因子必须大于0");
            }
            return stringRedisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Redis incr error: key={}", key, e);
            return null;
        }
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(大于0)
     * @return 减少后的值
     */
    public Long decr(String key, long delta) {
        try {
            if (delta < 0) {
                throw new RuntimeException("递减因子必须大于0");
            }
            return stringRedisTemplate.opsForValue().decrement(key, delta);
        } catch (Exception e) {
            log.error("Redis decr error: key={}", key, e);
            return null;
        }
    }

    // ==================== List 操作 ====================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     * @return list
     */
    public List<String> lGet(String key, long start, long end) {
        try {
            return stringRedisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("Redis lGet error: key={}", key, e);
            return null;
        }
    }

    /**
     * 将list放入缓存 (Left Push)
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean lPush(String key, String value) {
        try {
            stringRedisTemplate.opsForList().leftPush(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis lPush error: key={}", key, e);
            return false;
        }
    }

    /**
     * 从list中弹出 (Right Pop)
     *
     * @param key 键
     * @return 值
     */
    public String rPop(String key) {
        try {
            return stringRedisTemplate.opsForList().rightPop(key);
        } catch (Exception e) {
            log.error("Redis rPop error: key={}", key, e);
            return null;
        }
    }

    /**
     * 阻塞式弹出 (Right Pop with timeout)
     *
     * @param key     键
     * @param timeout 超时时间
     * @param unit    单位
     * @return 值
     */
    public String rPop(String key, long timeout, TimeUnit unit) {
        try {
            return stringRedisTemplate.opsForList().rightPop(key, timeout, unit);
        } catch (Exception e) {
            log.error("Redis rPop with timeout error: key={}", key, e);
            return null;
        }
    }

    // ==================== Lua 脚本 ====================

    /**
     * 执行 Lua 脚本
     *
     * @param script 脚本
     * @param keys   键列表
     * @param args   参数
     * @param <T>    返回类型
     * @return 结果
     */
    public <T> T execute(RedisScript<T> script, List<String> keys, Object... args) {
        try {
            return stringRedisTemplate.execute(script, keys, args);
        } catch (Exception e) {
            log.error("Redis execute script error", e);
            return null;
        }
    }

    /**
     * 带重试机制的操作执行
     *
     * @param operation 操作函数
     * @param <T>       返回类型
     * @return 结果
     */
    public <T> T executeWithRetry(RedisOperation<T> operation) {
        int retry = 0;
        Exception lastException = null;
        while (retry < DEFAULT_RETRY_TIMES) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                retry++;
                log.warn("Redis operation failed, retrying... ({}/{})", retry, DEFAULT_RETRY_TIMES);
                try {
                    Thread.sleep(100 * retry); // 简单的指数退避
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        log.error("Redis operation failed after {} retries", DEFAULT_RETRY_TIMES, lastException);
        return null;
    }

    /**
     * Redis操作接口
     */
    @FunctionalInterface
    public interface RedisOperation<T> {
        T execute() throws Exception;
    }
}
