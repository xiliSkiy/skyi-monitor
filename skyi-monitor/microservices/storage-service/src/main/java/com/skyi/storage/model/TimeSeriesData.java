package com.skyi.storage.model;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * 时间序列数据模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Measurement(name = "metric_data")
public class TimeSeriesData {

    /**
     * 时间戳
     */
    @Column(timestamp = true)
    private Instant time;
    
    /**
     * 资产ID
     */
    @Column(tag = true)
    private String assetId;
    
    /**
     * 指标名称
     */
    @Column(tag = true)
    private String metricName;
    
    /**
     * 指标值
     */
    @Column
    private Double value;
    
    /**
     * 单位
     */
    @Column(tag = true)
    private String unit;
    
    /**
     * 标签（附加信息）
     */
    @Builder.Default
    private Map<String, String> tags = null;
} 