package com.skyi.alert.service;

import java.util.List;
import java.util.Map;

/**
 * 消息发送服务接口
 */
public interface MessageService {

    /**
     * 发送对象消息
     *
     * @param topic   主题
     * @param data    数据
     * @param <T>     数据类型
     * @return 是否发送成功
     */
    <T> boolean sendMessage(String topic, T data);

    /**
     * 发送对象消息，并指定键
     *
     * @param topic   主题
     * @param key     消息键
     * @param data    数据
     * @param <T>     数据类型
     * @return 是否发送成功
     */
    <T> boolean sendMessage(String topic, String key, T data);

    /**
     * 发送批量对象消息
     *
     * @param topic    主题
     * @param dataList 数据列表
     * @param <T>      数据类型
     * @return 是否发送成功
     */
    <T> boolean sendBatchMessage(String topic, List<T> dataList);

    /**
     * 发送批量对象消息，并指定键
     *
     * @param topic    主题
     * @param key      消息键
     * @param dataList 数据列表
     * @param <T>      数据类型
     * @return 是否发送成功
     */
    <T> boolean sendBatchMessage(String topic, String key, List<T> dataList);

    /**
     * 发送Map消息
     *
     * @param topic   主题
     * @param data    Map数据
     * @return 是否发送成功
     */
    boolean sendMapMessage(String topic, Map<String, Object> data);

    /**
     * 发送Map消息，并指定键
     *
     * @param topic   主题
     * @param key     消息键
     * @param data    Map数据
     * @return 是否发送成功
     */
    boolean sendMapMessage(String topic, String key, Map<String, Object> data);
} 