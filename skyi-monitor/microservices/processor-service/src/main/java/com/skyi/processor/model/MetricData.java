package com.skyi.processor.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 指标数据模型
 * 用于存储指标数据
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MetricData {
    /**
     * 指标名称
     */
    private String metricName;
    
    /**
     * 指标值
     */
    private Double value;
    
    /**
     * 时间戳
     */
    private Instant timestamp;
    
    /**
     * 资产ID
     */
    private Long assetId;
    
    /**
     * 采集任务ID
     */
    private Long taskId;
    
    /**
     * 采集实例ID
     */
    private Long instanceId;
    
    /**
     * 标签，用于描述指标的维度
     */
    private Map<String, String> tags = new HashMap<>();
    
    /**
     * 额外字段，用于存储其他信息
     */
    private Map<String, Object> fields = new HashMap<>();
    
    /**
     * 添加标签
     *
     * @param key 标签键
     * @param value 标签值
     * @return 当前对象
     */
    public MetricData addTag(String key, String value) {
        if (this.tags == null) {
            this.tags = new HashMap<>();
        }
        this.tags.put(key, value);
        return this;
    }
    
    /**
     * 添加字段
     *
     * @param key 字段键
     * @param value 字段值
     * @return 当前对象
     */
    public MetricData addField(String key, Object value) {
        if (this.fields == null) {
            this.fields = new HashMap<>();
        }
        this.fields.put(key, value);
        return this;
    }
    
    /**
     * 创建指标数据
     *
     * @param metricName 指标名称
     * @param value 指标值
     * @param timestamp 时间戳
     * @return 指标数据对象
     */
    public static MetricData of(String metricName, Double value, Instant timestamp) {
        return new MetricData()
                .setMetricName(metricName)
                .setValue(value)
                .setTimestamp(timestamp);
    }
} 