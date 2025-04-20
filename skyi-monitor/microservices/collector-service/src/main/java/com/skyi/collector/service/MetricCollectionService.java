package com.skyi.collector.service;

import com.skyi.collector.dto.MetricDataDTO;
import com.skyi.collector.model.CollectorTask;
import com.skyi.collector.model.CollectorTaskInstance;
import com.skyi.collector.model.MetricDefinition;
import com.skyi.collector.model.MetricProtocolMapping;
import com.skyi.collector.repository.CollectorTaskInstanceRepository;
import com.skyi.collector.repository.MetricDefinitionRepository;
import com.skyi.collector.repository.MetricProtocolMappingRepository;
import com.skyi.collector.service.collector.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 指标采集服务
 * 协调指标定义、协议映射和采集器的工作，实现基于动态指标定义的采集功能
 */
@Slf4j
@Service
public class MetricCollectionService {
    
    @Autowired
    private MetricCollectorRegistry collectorRegistry;
    
    @Autowired
    private MetricDefinitionRepository metricDefinitionRepository;
    
    @Autowired
    private MetricProtocolMappingRepository metricProtocolMappingRepository;
    
    @Autowired
    private CollectorTaskInstanceRepository taskInstanceRepository;
    
    @Autowired
    private AssetService assetService; // 假设存在一个资产服务用于获取资产信息
    
    /**
     * 测试连接
     * 
     * @param protocol 协议
     * @param assetType 资产类型
     * @param connectionParams 连接参数
     * @return 连接测试结果
     */
    public ConnectionTestResult testConnection(String protocol, String assetType, Map<String, Object> connectionParams) {
        MetricCollector collector = collectorRegistry.findBestCollector(protocol, assetType);
        if (collector == null) {
            return ConnectionTestResult.failure("连接测试失败", "未找到支持的采集器: protocol=" + protocol + ", assetType=" + assetType);
        }
        
        return collector.testConnection(assetType, connectionParams);
    }
    
    /**
     * 执行任务实例
     * 
     * @param taskInstanceId 任务实例ID
     * @return 采集结果
     */
    public CollectionResult executeTaskInstance(Long taskInstanceId) {
        // 获取任务实例
        CollectorTaskInstance instance = taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new IllegalArgumentException("任务实例不存在: " + taskInstanceId));
        
        // 获取任务
        CollectorTask task = instance.getTask();
        if (task == null) {
            return CollectionResult.failure(LocalDateTime.now(), LocalDateTime.now(), "任务不存在");
        }
        
        // 检查任务状态
        if (task.getStatus() != 1) {
            return CollectionResult.failure(LocalDateTime.now(), LocalDateTime.now(), "任务已禁用");
        }
        
        // 获取资产信息
        Long assetId = task.getAssetId();
        Map<String, Object> assetInfo = assetService.getAssetBasicInfo(assetId);
        if (assetInfo == null || assetInfo.isEmpty()) {
            return CollectionResult.failure(LocalDateTime.now(), LocalDateTime.now(), "资产信息不存在: " + assetId);
        }
        
        // 解析任务中配置的指标ID列表
        List<Long> metricIds = parseMetricIds(task.getMetrics());
        if (metricIds.isEmpty()) {
            return CollectionResult.failure(LocalDateTime.now(), LocalDateTime.now(), "任务未配置有效的指标");
        }
        
        // 查询指标定义
        List<MetricDefinition> metricDefinitions = metricDefinitionRepository.findAllById(metricIds);
        if (metricDefinitions.isEmpty()) {
            return CollectionResult.failure(LocalDateTime.now(), LocalDateTime.now(), "未找到指标定义");
        }
        
        // 查询指标协议映射
        List<MetricProtocolMapping> protocolMappings = metricProtocolMappingRepository.findByMetricIdInAndProtocol(
                metricIds, task.getProtocol());
        
        // 构建映射关系
        Map<Long, CollectionContext.MetricProtocolMappingInfo> mappingInfoMap = buildMappingInfoMap(protocolMappings);
        
        // 创建采集上下文
        CollectionContext context = buildCollectionContext(task, instance, assetInfo, metricDefinitions, mappingInfoMap);
        
        // 查找最佳采集器
        MetricCollector collector = collectorRegistry.findBestCollector(task.getProtocol(), task.getType());
        if (collector == null) {
            return CollectionResult.failure(LocalDateTime.now(), LocalDateTime.now(), 
                    "未找到支持的采集器: protocol=" + task.getProtocol() + ", assetType=" + task.getType());
        }
        
        // 执行采集
        CollectionResult result;
        try {
            result = collector.collect(context);
            
            // 更新任务实例状态
            updateTaskInstanceStatus(instance, result);
            
            return result;
        } catch (Exception e) {
            log.error("采集执行出错: {}", e.getMessage(), e);
            
            // 更新任务实例状态为失败
            instance.setStatus(2); // 失败
            instance.setErrorMessage(e.getMessage());
            instance.setEndTime(LocalDateTime.now());
            taskInstanceRepository.save(instance);
            
            return CollectionResult.failure(context.getInstanceId() != null ? 
                    instance.getStartTime() : LocalDateTime.now(), 
                    LocalDateTime.now(), 
                    "采集执行出错: " + e.getMessage());
        }
    }
    
    /**
     * 解析任务中配置的指标ID列表
     */
    @SuppressWarnings("unchecked")
    private List<Long> parseMetricIds(String metrics) {
        List<Long> metricIds = new ArrayList<>();
        
        try {
            if (metrics == null || metrics.isBlank()) {
                return metricIds;
            }
            
            // 这里简化处理，实际应该使用ObjectMapper解析JSON
            // 假设metrics格式为: "[1,2,3]" 或 "[{\"id\":1},{\"id\":2}]"
            if (metrics.contains("\"id\"")) {
                // 格式为: "[{\"id\":1,\"enabled\":true},{\"id\":2,\"enabled\":true}]"
                // 解析为Map列表
                List<Map<String, Object>> metricsList = parseMetricsAsMapList(metrics);
                for (Map<String, Object> metric : metricsList) {
                    if (metric.containsKey("id")) {
                        Object id = metric.get("id");
                        if (id != null) {
                            metricIds.add(Long.valueOf(id.toString()));
                        }
                    }
                }
            } else {
                // 格式为: "[1,2,3]"
                // 解析为Long列表
                String cleanedMetrics = metrics.replace("[", "").replace("]", "").replace("\"", "");
                String[] idStrs = cleanedMetrics.split(",");
                for (String idStr : idStrs) {
                    if (!idStr.isBlank()) {
                        metricIds.add(Long.valueOf(idStr.trim()));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析指标ID列表出错: {}", e.getMessage(), e);
        }
        
        return metricIds;
    }
    
    /**
     * 解析复杂格式的指标配置
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseMetricsAsMapList(String metrics) {
        // 实际项目中应该使用ObjectMapper解析
        // 这里仅作示例，实际需要完整实现
        return new ArrayList<>();
    }
    
    /**
     * 构建指标协议映射信息Map
     */
    private Map<Long, CollectionContext.MetricProtocolMappingInfo> buildMappingInfoMap(
            List<MetricProtocolMapping> protocolMappings) {
        
        Map<Long, CollectionContext.MetricProtocolMappingInfo> mappingInfoMap = new HashMap<>();
        
        for (MetricProtocolMapping mapping : protocolMappings) {
            CollectionContext.MetricProtocolMappingInfo mappingInfo = CollectionContext.MetricProtocolMappingInfo.builder()
                    .id(mapping.getId())
                    .metricId(mapping.getMetricId())
                    .protocol(mapping.getProtocol())
                    .path(mapping.getPath())
                    .expression(mapping.getExpression())
                    .parameters(parseParameters(mapping.getParameters()))
                    .build();
            
            mappingInfoMap.put(mapping.getMetricId(), mappingInfo);
        }
        
        return mappingInfoMap;
    }
    
    /**
     * 解析参数字符串为Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseParameters(String parameters) {
        // 实际项目中应该使用ObjectMapper解析
        // 这里仅作示例，实际需要完整实现
        if (parameters == null || parameters.isBlank()) {
            return new HashMap<>();
        }
        
        try {
            // 假设格式为: "{\"key1\":\"value1\",\"key2\":123}"
            // 实际应使用ObjectMapper解析
            return new HashMap<>();
        } catch (Exception e) {
            log.warn("解析参数出错: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    /**
     * 构建采集上下文
     */
    private CollectionContext buildCollectionContext(
            CollectorTask task,
            CollectorTaskInstance instance,
            Map<String, Object> assetInfo,
            List<MetricDefinition> metricDefinitions,
            Map<Long, CollectionContext.MetricProtocolMappingInfo> mappingInfoMap) {
        
        // 解析连接参数
        Map<String, Object> connectionParams = parseConnectionParams(task.getConnectionParams());
        
        return CollectionContext.builder()
                .taskId(task.getId())
                .instanceId(instance.getId())
                .assetId(task.getAssetId())
                .assetType(task.getType())
                .assetName(getStringValue(assetInfo, "name"))
                .assetIp(getStringValue(assetInfo, "ip"))
                .metrics(metricDefinitions)
                .protocolMappings(mappingInfoMap)
                .connectionParams(connectionParams)
                .variables(new HashMap<>())
                .originalTask(task)
                .build();
    }
    
    /**
     * 解析连接参数字符串为Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseConnectionParams(String connectionParams) {
        // 实际项目中应该使用ObjectMapper解析
        // 这里仅作示例，实际需要完整实现
        if (connectionParams == null || connectionParams.isBlank()) {
            return new HashMap<>();
        }
        
        try {
            // 假设格式为: "{\"ipAddress\":\"192.168.1.1\",\"port\":161,\"community\":\"public\"}"
            // 实际应使用ObjectMapper解析
            return new HashMap<>();
        } catch (Exception e) {
            log.warn("解析连接参数出错: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    /**
     * 从Map中获取字符串值
     */
    private String getStringValue(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return null;
        }
        
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * 更新任务实例状态
     */
    private void updateTaskInstanceStatus(CollectorTaskInstance instance, CollectionResult result) {
        // 设置结束时间
        instance.setEndTime(result.getEndTime());
        
        // 设置状态
        if (result.isSuccess()) {
            instance.setStatus(1); // 成功
            instance.setDataPointCount(result.getMetricData().size());
        } else {
            instance.setStatus(2); // 失败
            instance.setErrorMessage(result.getErrorMessage());
        }
        
        // 保存任务实例
        taskInstanceRepository.save(instance);
    }
} 