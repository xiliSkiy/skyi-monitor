package com.skyi.collector.service.collector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyi.collector.dto.CollectorTaskDTO;
import com.skyi.collector.dto.MetricDataDTO;
import com.skyi.collector.model.CollectorTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 抽象采集器
 */
@Slf4j
public abstract class AbstractCollector implements Collector {
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    @Override
    public boolean supports(CollectorTask task) {
        return getType().equals(task.getType()) && getSupportedProtocols().contains(task.getProtocol());
    }
    
    /**
     * 解析任务中的指标配置
     * 
     * @param task 采集任务
     * @return 指标配置列表
     */
    protected List<CollectorTaskDTO.MetricDTO> parseMetrics(CollectorTask task) {
        try {
            return objectMapper.readValue(task.getMetrics(), new TypeReference<List<CollectorTaskDTO.MetricDTO>>() {});
        } catch (JsonProcessingException e) {
            log.error("解析指标配置出错: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 解析任务中的连接参数
     * 
     * @param task 采集任务
     * @return 连接参数
     */
    protected Map<String, String> parseConnectionParams(CollectorTask task) {
        try {
            return objectMapper.readValue(task.getConnectionParams(), new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            log.error("解析连接参数出错: {}", e.getMessage(), e);
            return Map.of();
        }
    }
    
    /**
     * 创建指标数据
     * 
     * @param task 采集任务
     * @param instanceId 任务实例ID
     * @param metricName 指标名称
     * @param metricValue 指标值
     * @param metricLabels 指标标签
     * @return 指标数据
     */
    protected MetricDataDTO createMetricData(
            CollectorTask task, 
            Long instanceId, 
            String metricName, 
            Double metricValue, 
            Map<String, String> metricLabels) {
        
        MetricDataDTO metricData = new MetricDataDTO();
        metricData.setTaskId(task.getId());
        metricData.setInstanceId(instanceId);
        metricData.setAssetId(task.getAssetId());
        metricData.setMetricName(metricName);
        metricData.setMetricValue(metricValue);
        metricData.setMetricLabels(metricLabels);
        metricData.setCollectTime(LocalDateTime.now());
        
        return metricData;
    }
} 