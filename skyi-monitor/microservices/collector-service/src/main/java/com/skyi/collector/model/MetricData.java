package com.skyi.collector.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 指标数据模型
 * 用于存储采集到的指标数据
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MetricData implements Serializable {
    private static final long serialVersionUID = 1L;

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
    private Long timestamp;

    /**
     * 采集任务ID
     */
    private Long taskId;

    /**
     * 采集实例ID
     */
    private Long instanceId;

    /**
     * 资产ID
     */
    private Long assetId;

    /**
     * 标签
     * 用于存储与指标相关的标签信息，例如主机名、IP等
     */
    private Map<String, String> tags = new HashMap<>();

    /**
     * 额外字段
     * 用于存储与指标相关的额外信息
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
        if (key != null && value != null) {
            this.tags.put(key, value);
        }
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
        if (key != null && value != null) {
            this.fields.put(key, value);
        }
        return this;
    }

    /**
     * 创建指标数据
     *
     * @param metricName 指标名称
     * @param value 指标值
     * @param timestamp 时间戳
     * @return 指标数据
     */
    public static MetricData of(String metricName, Double value, Long timestamp) {
        return new MetricData()
                .setMetricName(metricName)
                .setValue(value)
                .setTimestamp(timestamp);
    }
} 