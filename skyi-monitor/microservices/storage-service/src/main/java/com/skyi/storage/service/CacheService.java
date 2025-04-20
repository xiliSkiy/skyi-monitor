package com.skyi.storage.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 */
public interface CacheService {

    /**
     * 设置缓存
     *
     * @param key   缓存键
     * @param value 缓存值
     * @return 是否设置成功
     */
    boolean set(String key, Object value);

    /**
     * 设置缓存并设置过期时间
     *
     * @param key     缓存键
     * @param value   缓存值
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 是否设置成功
     */
    boolean set(String key, Object value, long timeout, TimeUnit unit);

    /**
     * 获取缓存
     *
     * @param key 缓存键
     * @return 缓存值
     */
    Object get(String key);

    /**
     * 获取缓存并转换为指定类型
     *
     * @param key   缓存键
     * @param clazz 目标类型
     * @return 缓存值
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 删除缓存
     *
     * @param key 缓存键
     * @return 是否删除成功
     */
    boolean delete(String key);

    /**
     * 批量删除缓存
     *
     * @param pattern 键模式
     * @return 删除的数量
     */
    long deleteByPattern(String pattern);

    /**
     * 设置缓存过期时间
     *
     * @param key     缓存键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 是否设置成功
     */
    boolean expire(String key, long timeout, TimeUnit unit);

    /**
     * 判断缓存是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    boolean hasKey(String key);

    /**
     * 获取缓存过期时间
     *
     * @param key  缓存键
     * @param unit 时间单位
     * @return 过期时间
     */
    long getExpire(String key, TimeUnit unit);

    /**
     * 设置Hash缓存
     *
     * @param key     缓存键
     * @param hashKey Hash键
     * @param value   缓存值
     * @return 是否设置成功
     */
    boolean hashSet(String key, String hashKey, Object value);

    /**
     * 获取Hash缓存
     *
     * @param key     缓存键
     * @param hashKey Hash键
     * @return 缓存值
     */
    Object hashGet(String key, String hashKey);

    /**
     * 设置Hash缓存
     *
     * @param key 缓存键
     * @param map Hash映射
     * @return 是否设置成功
     */
    boolean hashSetAll(String key, Map<String, Object> map);

    /**
     * 获取Hash所有键值
     *
     * @param key 缓存键
     * @return Hash映射
     */
    Map<Object, Object> hashGetAll(String key);

    /**
     * 删除Hash缓存
     *
     * @param key      缓存键
     * @param hashKeys Hash键数组
     * @return 删除的数量
     */
    long hashDelete(String key, Object... hashKeys);

    /**
     * 判断Hash缓存是否存在
     *
     * @param key     缓存键
     * @param hashKey Hash键
     * @return 是否存在
     */
    boolean hashHasKey(String key, String hashKey);

    /**
     * 往List缓存添加元素
     *
     * @param key   缓存键
     * @param value 元素值
     * @return 列表长度
     */
    long listPush(String key, Object value);

    /**
     * 往List缓存添加多个元素
     *
     * @param key    缓存键
     * @param values 元素值数组
     * @return 列表长度
     */
    long listPushAll(String key, Object... values);

    /**
     * 从List缓存获取元素范围
     *
     * @param key   缓存键
     * @param start 起始索引
     * @param end   结束索引
     * @return 元素列表
     */
    List<Object> listRange(String key, long start, long end);

    /**
     * 获取Set缓存
     *
     * @param key 缓存键
     * @return 元素集合
     */
    Set<Object> setMembers(String key);

    /**
     * 往Set缓存添加元素
     *
     * @param key    缓存键
     * @param values 元素值数组
     * @return 添加的数量
     */
    long setAdd(String key, Object... values);

    /**
     * 从Set缓存移除元素
     *
     * @param key    缓存键
     * @param values 元素值数组
     * @return 移除的数量
     */
    long setRemove(String key, Object... values);

    /**
     * 判断Set缓存是否包含元素
     *
     * @param key   缓存键
     * @param value 元素值
     * @return 是否包含
     */
    boolean setContains(String key, Object value);

    /**
     * 获取Set缓存大小
     *
     * @param key 缓存键
     * @return 集合大小
     */
    long setSize(String key);
} 