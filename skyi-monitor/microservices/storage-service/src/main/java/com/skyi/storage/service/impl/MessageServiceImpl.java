package com.skyi.storage.service.impl;

import com.skyi.storage.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 消息发送服务实现类
 */
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public <T> boolean sendMessage(String topic, T data) {
        return sendMessage(topic, null, data);
    }

    @Override
    public <T> boolean sendMessage(String topic, String key, T data) {
        try {
            ListenableFuture<SendResult<String, Object>> future;
            if (key != null && !key.isEmpty()) {
                future = kafkaTemplate.send(topic, key, data);
            } else {
                future = kafkaTemplate.send(topic, data);
            }

            // 添加回调处理
            future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
                @Override
                public void onFailure(Throwable ex) {
                    log.error("发送消息到主题[{}]失败: {}", topic, ex.getMessage());
                }

                @Override
                public void onSuccess(SendResult<String, Object> result) {
                    log.debug("发送消息到主题[{}]成功: {}", topic, result.getRecordMetadata().offset());
                }
            });

            // 同步等待结果，最多等待5秒
            future.get(5, TimeUnit.SECONDS);
            return true;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("发送消息到主题[{}]异常: {}", topic, e.getMessage());
            return false;
        }
    }

    @Override
    public <T> boolean sendBatchMessage(String topic, List<T> dataList) {
        return sendBatchMessage(topic, null, dataList);
    }

    @Override
    public <T> boolean sendBatchMessage(String topic, String key, List<T> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            log.warn("批量发送消息到主题[{}]失败: 数据列表为空", topic);
            return false;
        }

        try {
            for (T data : dataList) {
                if (key != null && !key.isEmpty()) {
                    kafkaTemplate.send(topic, key, data);
                } else {
                    kafkaTemplate.send(topic, data);
                }
            }
            // 发送后同步刷新，确保消息已被发送
            kafkaTemplate.flush();
            return true;
        } catch (Exception e) {
            log.error("批量发送消息到主题[{}]异常: {}", topic, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendMapMessage(String topic, Map<String, Object> data) {
        return sendMapMessage(topic, null, data);
    }

    @Override
    public boolean sendMapMessage(String topic, String key, Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            log.warn("发送Map消息到主题[{}]失败: 数据为空", topic);
            return false;
        }

        try {
            ListenableFuture<SendResult<String, Object>> future;
            if (key != null && !key.isEmpty()) {
                future = kafkaTemplate.send(topic, key, data);
            } else {
                future = kafkaTemplate.send(topic, data);
            }

            // 添加回调处理
            future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
                @Override
                public void onFailure(Throwable ex) {
                    log.error("发送Map消息到主题[{}]失败: {}", topic, ex.getMessage());
                }

                @Override
                public void onSuccess(SendResult<String, Object> result) {
                    log.debug("发送Map消息到主题[{}]成功: {}", topic, result.getRecordMetadata().offset());
                }
            });

            // 同步等待结果，最多等待5秒
            future.get(5, TimeUnit.SECONDS);
            return true;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("发送Map消息到主题[{}]异常: {}", topic, e.getMessage());
            return false;
        }
    }
} 