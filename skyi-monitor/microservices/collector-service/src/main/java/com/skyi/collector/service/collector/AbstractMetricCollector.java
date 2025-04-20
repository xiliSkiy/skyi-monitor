package com.skyi.collector.service.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyi.collector.dto.MetricDataDTO;
import com.skyi.collector.model.MetricDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 抽象指标采集器
 * 实现了MetricCollector接口的通用功能
 */
@Slf4j
public abstract class AbstractMetricCollector implements MetricCollector {
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    @Override
    public boolean supportsMetric(MetricDefinition metricDefinition) {
        return getProtocol().equalsIgnoreCase(metricDefinition.getCollectionMethod()) &&
               getSupportedAssetTypes().contains(metricDefinition.getCategory());
    }
    
    /**
     * 创建指标数据对象
     * 
     * @param context 采集上下文
     * @param metricDef 指标定义
     * @param value 指标值
     * @param labels 标签Map
     * @return 指标数据DTO
     */
    protected MetricDataDTO createMetricData(
            CollectionContext context,
            MetricDefinition metricDef,
            Double value,
            Map<String, String> labels) {
        
        MetricDataDTO data = new MetricDataDTO();
        data.setTaskId(context.getTaskId());
        data.setInstanceId(context.getInstanceId());
        data.setAssetId(context.getAssetId());
        data.setMetricName(metricDef.getName());
        data.setMetricValue(value);
        
        // 如果没有提供标签，则创建一个标签Map
        if (labels == null) {
            labels = new HashMap<>();
        }
        
        // 添加通用标签
        if (!labels.containsKey("asset_id")) {
            labels.put("asset_id", String.valueOf(context.getAssetId()));
        }
        if (!labels.containsKey("asset_name")) {
            labels.put("asset_name", context.getAssetName());
        }
        if (!labels.containsKey("metric_id")) {
            labels.put("metric_id", String.valueOf(metricDef.getId()));
        }
        if (!labels.containsKey("metric_code")) {
            labels.put("metric_code", metricDef.getCode());
        }
        if (!labels.containsKey("unit")) {
            labels.put("unit", metricDef.getUnit());
        }
        
        data.setMetricLabels(labels);
        data.setCollectTime(LocalDateTime.now());
        
        return data;
    }
    
    /**
     * 获取指标的协议映射路径
     * 
     * @param context 采集上下文
     * @param metricDef 指标定义
     * @return 协议映射路径
     */
    protected String getMetricPath(CollectionContext context, MetricDefinition metricDef) {
        CollectionContext.MetricProtocolMappingInfo mapping = 
                context.getProtocolMappings().get(metricDef.getId());
        
        if (mapping == null) {
            log.warn("未找到指标[{}]的协议映射信息", metricDef.getCode());
            return null;
        }
        
        return mapping.getPath();
    }
    
    /**
     * 获取指标的额外参数
     * 
     * @param context 采集上下文
     * @param metricDef 指标定义
     * @return 额外参数Map
     */
    protected Map<String, Object> getMetricParameters(CollectionContext context, MetricDefinition metricDef) {
        CollectionContext.MetricProtocolMappingInfo mapping = 
                context.getProtocolMappings().get(metricDef.getId());
        
        if (mapping == null || mapping.getParameters() == null) {
            return new HashMap<>();
        }
        
        return mapping.getParameters();
    }
    
    /**
     * 解析脚本表达式
     * 
     * @param context 采集上下文
     * @param metricDef 指标定义
     * @param rawValue 原始值
     * @return 解析后的值
     */
    protected Double parseExpression(CollectionContext context, MetricDefinition metricDef, Object rawValue) {
        CollectionContext.MetricProtocolMappingInfo mapping = 
                context.getProtocolMappings().get(metricDef.getId());
        
        if (mapping == null || mapping.getExpression() == null || mapping.getExpression().isBlank()) {
            // 没有表达式，尝试直接转换为Double
            try {
                if (rawValue instanceof Number) {
                    return ((Number) rawValue).doubleValue();
                } else if (rawValue instanceof String) {
                    return Double.parseDouble((String) rawValue);
                } else {
                    return null;
                }
            } catch (Exception e) {
                log.warn("无法将值[{}]转换为Double: {}", rawValue, e.getMessage());
                return null;
            }
        }
        
        // TODO: 实现表达式解析引擎，支持如JavaScript、Groovy等脚本语言
        // 这里暂时实现为直接返回原始值的Double表示
        try {
            if (rawValue instanceof Number) {
                return ((Number) rawValue).doubleValue();
            } else if (rawValue instanceof String) {
                return Double.parseDouble((String) rawValue);
            } else {
                return null;
            }
        } catch (Exception e) {
            log.warn("无法解析表达式[{}]或将值[{}]转换为Double: {}", 
                    mapping.getExpression(), rawValue, e.getMessage());
            return null;
        }
    }
    
    /**
     * 处理采集异常
     * 
     * @param e 异常
     * @param startTime 开始时间
     * @param message 错误信息
     * @return 采集结果
     */
    protected CollectionResult handleCollectionException(Exception e, 
                                                        LocalDateTime startTime,
                                                        String message) {
        log.error(message + ": {}", e.getMessage(), e);
        return CollectionResult.failure(startTime, LocalDateTime.now(), 
                message + ": " + e.getMessage());
    }
    
    /**
     * 预处理采集上下文
     * 子类可以重写此方法进行特定的上下文预处理
     * 
     * @param context 采集上下文
     */
    protected void preProcess(CollectionContext context) {
        // 默认实现为空，子类可以重写此方法
    }
    
    /**
     * 后处理采集结果
     * 子类可以重写此方法进行特定的结果后处理
     * 
     * @param context 采集上下文
     * @param result 采集结果
     * @return 处理后的采集结果
     */
    protected CollectionResult postProcess(CollectionContext context, CollectionResult result) {
        // 默认实现为返回原始结果，子类可以重写此方法
        return result;
    }
} 