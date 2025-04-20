package com.skyi.collector.service.impl;

import com.skyi.collector.dto.MetricDataDTO;
import com.skyi.collector.service.collector.CollectionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 指标数据生产者服务
 * 负责将采集到的指标数据发送到Kafka
 */
@Slf4j
@Service
public class MetricDataProducerService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    @Qualifier("metricDataTopicName")
    private String metricDataTopic;
    
    @Autowired
    @Qualifier("collectionStatusTopicName")
    private String collectionStatusTopic;
    
    @Value("${collector.kafka.batch-size:100}")
    private int batchSize;
    
    @Value("${spring.kafka.producer.retries:3}")
    private int retries;
    
    // 发送统计
    private final AtomicLong totalSent = new AtomicLong(0);
    private final AtomicLong successSent = new AtomicLong(0);
    private final AtomicLong failedSent = new AtomicLong(0);
    
    @PostConstruct
    public void init() {
        log.info("初始化指标数据生产者服务，指标数据主题: {}, 采集状态主题: {}", metricDataTopic, collectionStatusTopic);
    }
    
    /**
     * 发送指标数据到Kafka
     *
     * @param metricData 指标数据
     * @return 发送是否成功
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public boolean sendMetricData(MetricDataDTO metricData) {
        if (metricData == null) {
            log.warn("发送指标数据失败，数据为空");
            return false;
        }
        
        try {
            totalSent.incrementAndGet();
            
            // 转换为Kafka友好的格式（处理LocalDateTime）
            Map<String, Object> data = convertToKafkaFormat(metricData);
            String key = metricData.getAssetId() + "_" + metricData.getMetricName();
            
            // 发送消息
            ListenableFuture<SendResult<String, Object>> future = kafkaTemplate.send(metricDataTopic, key, data);
            
            // 异步处理发送结果
            future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
                @Override
                public void onFailure(Throwable ex) {
                    failedSent.incrementAndGet();
                    log.error("发送指标数据到Kafka失败，metricName={}，assetId={}，错误：{}", 
                            metricData.getMetricName(), metricData.getAssetId(), ex.getMessage(), ex);
                }
                
                @Override
                public void onSuccess(SendResult<String, Object> result) {
                    successSent.incrementAndGet();
                    log.debug("成功发送指标数据到Kafka，metricName={}，assetId={}，partition={}，offset={}", 
                            metricData.getMetricName(), metricData.getAssetId(),
                            result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                }
            });
            
            return true;
        } catch (Exception e) {
            failedSent.incrementAndGet();
            log.error("发送指标数据异常，metricName={}，assetId={}", 
                    metricData.getMetricName(), metricData.getAssetId(), e);
            
            if (retries > 0) {
                throw e; // 抛出异常以触发重试
            }
            return false;
        }
    }
    
    /**
     * 发送采集结果状态到Kafka
     *
     * @param taskId 任务ID
     * @param instanceId 实例ID
     * @param assetId 资产ID
     * @param result 采集结果
     * @return 发送是否成功
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public boolean sendCollectionStatus(Long taskId, Long instanceId, Long assetId, CollectionResult result) {
        if (result == null) {
            log.warn("发送采集状态失败，结果为空");
            return false;
        }
        
        try {
            // 构建状态数据
            Map<String, Object> statusData = new HashMap<>();
            statusData.put("taskId", taskId);
            statusData.put("instanceId", instanceId);
            statusData.put("assetId", assetId);
            statusData.put("success", result.isSuccess());
            statusData.put("startTime", result.getStartTime() != null ? 
                    result.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null);
            statusData.put("endTime", result.getEndTime() != null ? 
                    result.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null);
            statusData.put("successCount", result.getSuccessCount());
            statusData.put("failCount", result.getFailCount());
            statusData.put("errorMessage", result.getErrorMessage());
            statusData.put("timestamp", Instant.now().toEpochMilli());
            
            // 发送消息
            String key = taskId + "_" + instanceId;
            ListenableFuture<SendResult<String, Object>> future = kafkaTemplate.send(collectionStatusTopic, key, statusData);
            
            // 异步处理发送结果
            future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
                @Override
                public void onFailure(Throwable ex) {
                    log.error("发送采集状态到Kafka失败，taskId={}，instanceId={}，错误：{}", 
                            taskId, instanceId, ex.getMessage(), ex);
                }
                
                @Override
                public void onSuccess(SendResult<String, Object> result) {
                    log.debug("成功发送采集状态到Kafka，taskId={}，instanceId={}", taskId, instanceId);
                }
            });
            
            return true;
        } catch (Exception e) {
            log.error("发送采集状态异常，taskId={}，instanceId={}", taskId, instanceId, e);
            
            if (retries > 0) {
                throw e; // 抛出异常以触发重试
            }
            return false;
        }
    }
    
    /**
     * 批量发送指标数据到Kafka
     *
     * @param metricDataList 指标数据列表
     * @return 成功发送的数量
     */
    public int sendMetricDataBatch(Iterable<MetricDataDTO> metricDataList) {
        if (metricDataList == null) {
            return 0;
        }
        
        // 分批处理
        List<MetricDataDTO> batch = new ArrayList<>(batchSize);
        int totalSuccess = 0;
        
        for (MetricDataDTO metricData : metricDataList) {
            batch.add(metricData);
            
            // 达到批处理大小，发送批次
            if (batch.size() >= batchSize) {
                totalSuccess += processBatch(batch);
                batch.clear();
            }
        }
        
        // 处理剩余的批次
        if (!batch.isEmpty()) {
            totalSuccess += processBatch(batch);
        }
        
        log.info("批量发送指标数据完成，成功：{}", totalSuccess);
        return totalSuccess;
    }
    
    /**
     * 处理单个批次
     */
    private int processBatch(List<MetricDataDTO> batch) {
        int successCount = 0;
        for (MetricDataDTO metricData : batch) {
            try {
                if (sendMetricData(metricData)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批量发送指标数据时单条处理失败：{}", e.getMessage());
            }
        }
        return successCount;
    }
    
    /**
     * 获取发送统计信息
     */
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalSent", totalSent.get());
        stats.put("successSent", successSent.get());
        stats.put("failedSent", failedSent.get());
        return stats;
    }
    
    /**
     * 将MetricDataDTO转换为Kafka友好的格式
     * 主要是处理LocalDateTime转换为时间戳
     *
     * @param metricData 指标数据
     * @return Kafka友好的格式
     */
    private Map<String, Object> convertToKafkaFormat(MetricDataDTO metricData) {
        Map<String, Object> data = new HashMap<>();
        
        data.put("id", metricData.getId());
        data.put("taskId", metricData.getTaskId());
        data.put("instanceId", metricData.getInstanceId());
        data.put("assetId", metricData.getAssetId());
        data.put("metricName", metricData.getMetricName());
        data.put("metricLabels", metricData.getMetricLabels());
        data.put("metricValue", metricData.getMetricValue());
        
        // 转换时间为时间戳
        if (metricData.getCollectTime() != null) {
            data.put("collectTime", metricData.getCollectTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        
        if (metricData.getCreateTime() != null) {
            data.put("createTime", metricData.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        } else {
            data.put("createTime", Instant.now().toEpochMilli());
        }
        
        return data;
    }
} 