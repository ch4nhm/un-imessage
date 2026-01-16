package com.unimessage.cache;

import com.unimessage.util.RedisUtil;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * 统一缓存服务
 * 负责所有缓存操作的入口，规范化键生成和操作逻辑
 *
 * @author 海明
 * @since 2026-01-14
 */
@Getter
@Slf4j
@Service
public class CacheService {

    /**
     * -- GETTER --
     * 获取RedisUtil实例 (用于特殊操作如Lua脚本)
     */
    @Resource
    private RedisUtil redisUtil;

    // ==================== 键管理 ====================

    /**
     * 生成标准缓存键
     *
     * @param prefix 定义在 CacheKeyConstants 中的前缀
     * @param parts  动态部分（如ID、Code等）
     * @return 完整缓存键
     */
    public String buildKey(String prefix, String... parts) {
        if (parts == null || parts.length == 0) {
            return prefix;
        }
        StringBuilder sb = new StringBuilder(prefix);
        for (String part : parts) {
            sb.append(part);
        }
        return sb.toString();
    }

    // ==================== 基础操作封装 ====================

    /**
     * 设置缓存
     *
     * @param key   键
     * @param value 值
     * @return 是否成功
     */
    public boolean set(String key, String value) {
        return redisUtil.set(key, value);
    }

    /**
     * 设置缓存（带过期时间）
     *
     * @param key      键
     * @param value    值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     * @return 是否成功
     */
    public boolean set(String key, String value, long timeout, TimeUnit timeUnit) {
        return redisUtil.set(key, value, timeout, timeUnit);
    }

    /**
     * 设置对象缓存（自动序列化）
     *
     * @param key   键
     * @param value 对象
     * @return 是否成功
     */
    public boolean setObject(String key, Object value) {
        return redisUtil.setObject(key, value);
    }

    /**
     * 设置对象缓存（带过期时间）
     *
     * @param key      键
     * @param value    对象
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     * @return 是否成功
     */
    public boolean setObject(String key, Object value, long timeout, TimeUnit timeUnit) {
        return redisUtil.setObject(key, value, timeout, timeUnit);
    }

    /**
     * 获取缓存
     *
     * @param key 键
     * @return 值
     */
    public String get(String key) {
        return redisUtil.get(key);
    }

    /**
     * 获取对象缓存（自动反序列化）
     *
     * @param key   键
     * @param clazz 对象类型
     * @param <T>   泛型
     * @return 对象
     */
    public <T> T getObject(String key, Class<T> clazz) {
        return redisUtil.getObject(key, clazz);
    }

    /**
     * 删除缓存
     *
     * @param key 键
     */
    public void delete(String key) {
        redisUtil.del(key);
    }

    /**
     * 批量删除缓存
     *
     * @param keys 键集合
     */
    public void delete(Collection<String> keys) {
        redisUtil.del(keys);
    }

    /**
     * 设置过期时间
     *
     * @param key      键
     * @param timeout  时间
     * @param timeUnit 单位
     * @return 是否成功
     */
    public boolean expire(String key, long timeout, TimeUnit timeUnit) {
        return redisUtil.expire(key, timeout, timeUnit);
    }

    /**
     * 检查键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        return redisUtil.hasKey(key);
    }

    /**
     * 递增
     */
    public Long increment(String key, long delta) {
        return redisUtil.incr(key, delta);
    }

    /**
     * 递减
     */
    public Long decrement(String key, long delta) {
        return redisUtil.decr(key, delta);
    }

    // ==================== 队列操作 ====================

    /**
     * 入队
     */
    public boolean lPush(String key, String value) {
        return redisUtil.lPush(key, value);
    }

    /**
     * 出队 (非阻塞)
     */
    public String rPop(String key) {
        return redisUtil.rPop(key);
    }

    /**
     * 出队 (阻塞)
     */
    public String rPop(String key, long timeout, TimeUnit unit) {
        return redisUtil.rPop(key, timeout, unit);
    }

    // ==================== 高级特性 ====================

    /**
     * 获取缓存（带降级默认值）
     * 当缓存获取失败或为null时，返回默认值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 结果
     */
    public String getOrDefault(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 如果不存在则设置（用于幂等性校验）
     *
     * @param key     键
     * @param value   值
     * @param seconds 过期时间（秒）
     * @return true 设置成功（键不存在），false 键已存在
     */
    public boolean setIfAbsent(String key, String value, long seconds) {
        return redisUtil.setIfAbsent(key, value, seconds);
    }

}
