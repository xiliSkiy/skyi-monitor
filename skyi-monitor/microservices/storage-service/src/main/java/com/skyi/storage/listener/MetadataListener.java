package com.skyi.storage.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyi.storage.constant.KafkaTopicConstants;
import com.skyi.storage.dto.MetadataDTO;
import com.skyi.storage.service.CacheService;
import com.skyi.storage.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 元数据消息监听器
 */
@Slf4j
@Component
public class MetadataListener {

    @Autowired
    private MetadataService metadataService;
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 处理元数据保存消息
     */
    @KafkaListener(topics = KafkaTopicConstants.TOPIC_METADATA_SAVE)
    public void handleMetadataSave(
            @Payload MetadataDTO data,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        
        log.info("接收到元数据保存消息: topic={}, partition={}, offset={}, data={}", 
                topic, partition, offset, data);
        
        try {
            MetadataDTO savedData = metadataService.saveMetadata(data);
            if (savedData != null) {
                log.info("元数据保存成功: id={}", savedData.getId());
                // 更新缓存
                updateMetadataCache(savedData);
                ack.acknowledge();
            } else {
                log.error("元数据保存失败，将重试");
            }
        } catch (Exception e) {
            log.error("处理元数据保存异常", e);
            if (isRetryableException(e)) {
                log.warn("发生可重试异常，消息将被重新处理");
            } else {
                log.error("发生不可重试异常，消息将被丢弃");
                ack.acknowledge();
            }
        }
    }

    /**
     * 处理元数据更新消息
     */
    @KafkaListener(topics = KafkaTopicConstants.TOPIC_METADATA_UPDATE)
    public void handleMetadataUpdate(
            @Payload MetadataDTO data,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        
        log.info("接收到元数据更新消息: topic={}, partition={}, offset={}, data={}", 
                topic, partition, offset, data);
        
        try {
            // 确保ID不为空
            if (data.getId() == null) {
                log.error("元数据更新失败，ID不能为空");
                ack.acknowledge(); // 无法处理的消息，直接确认
                return;
            }
            
            MetadataDTO updatedData = metadataService.updateMetadata(data);
            if (updatedData != null) {
                log.info("元数据更新成功: id={}", updatedData.getId());
                // 更新缓存
                updateMetadataCache(updatedData);
                ack.acknowledge();
            } else {
                log.error("元数据更新失败，将重试");
            }
        } catch (Exception e) {
            log.error("处理元数据更新异常", e);
            if (isRetryableException(e)) {
                log.warn("发生可重试异常，消息将被重新处理");
            } else {
                log.error("发生不可重试异常，消息将被丢弃");
                ack.acknowledge();
            }
        }
    }

    /**
     * 处理元数据删除消息
     */
    @KafkaListener(topics = KafkaTopicConstants.TOPIC_METADATA_DELETE)
    public void handleMetadataDelete(
            @Payload Map<String, Object> data,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        
        log.info("接收到元数据删除消息: topic={}, partition={}, offset={}, data={}", 
                topic, partition, offset, data);
        
        try {
            boolean result = false;
            
            // 根据不同的删除类型执行不同的删除操作
            if (data.containsKey("id")) {
                Long id = Long.valueOf(data.get("id").toString());
                result = metadataService.deleteMetadata(id);
                // 清除缓存
                cacheService.delete("metadata:id:" + id);
            } else if (data.containsKey("type") && data.containsKey("refId")) {
                String type = data.get("type").toString();
                String refId = data.get("refId").toString();
                
                if (data.containsKey("metaKey")) {
                    String metaKey = data.get("metaKey").toString();
                    result = metadataService.deleteMetadataByTypeAndRefIdAndKey(type, refId, metaKey);
                    // 清除缓存
                    cacheService.delete("metadata:type:" + type + ":refId:" + refId + ":key:" + metaKey);
                } else {
                    result = metadataService.deleteMetadataByTypeAndRefId(type, refId);
                    // 清除缓存
                    cacheService.deleteByPattern("metadata:type:" + type + ":refId:" + refId + "*");
                }
            }
            
            if (result) {
                log.info("元数据删除成功");
                ack.acknowledge();
            } else {
                log.error("元数据删除失败，将重试");
            }
        } catch (Exception e) {
            log.error("处理元数据删除异常", e);
            if (isRetryableException(e)) {
                log.warn("发生可重试异常，消息将被重新处理");
            } else {
                log.error("发生不可重试异常，消息将被丢弃");
                ack.acknowledge();
            }
        }
    }

    /**
     * 处理缓存失效消息
     */
    @KafkaListener(topics = KafkaTopicConstants.TOPIC_CACHE_INVALIDATE)
    public void handleCacheInvalidate(
            @Payload Map<String, Object> data,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        
        log.info("接收到缓存失效消息: topic={}, partition={}, offset={}, data={}", 
                topic, partition, offset, data);
        
        try {
            if (data.containsKey("key")) {
                String key = data.get("key").toString();
                cacheService.delete(key);
                log.info("缓存删除成功: key={}", key);
            } else if (data.containsKey("pattern")) {
                String pattern = data.get("pattern").toString();
                long count = cacheService.deleteByPattern(pattern);
                log.info("批量缓存删除成功: pattern={}, count={}", pattern, count);
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("处理缓存失效异常", e);
            ack.acknowledge(); // 缓存操作通常不需要重试
        }
    }

    /**
     * 更新元数据缓存
     */
    private void updateMetadataCache(MetadataDTO metadata) {
        try {
            // 按ID缓存
            String idKey = "metadata:id:" + metadata.getId();
            cacheService.set(idKey, metadata, 1, TimeUnit.HOURS);
            
            // 按类型、关联ID和元数据键缓存
            String keyPath = "metadata:type:" + metadata.getType() + ":refId:" + metadata.getRefId() + ":key:" + metadata.getMetaKey();
            cacheService.set(keyPath, metadata, 1, TimeUnit.HOURS);
            
            log.debug("元数据缓存更新成功: id={}", metadata.getId());
        } catch (Exception e) {
            log.error("更新元数据缓存失败", e);
        }
    }

    /**
     * 判断异常是否可重试
     */
    private boolean isRetryableException(Exception e) {
        return e instanceof java.net.ConnectException
                || e instanceof java.net.SocketTimeoutException
                || e instanceof java.io.IOException
                || e.getMessage() != null && e.getMessage().contains("timed out");
    }
} 