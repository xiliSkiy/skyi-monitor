package com.skyi.visualization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * 实时数据推送DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealTimeDataDTO {
    /**
     * 组件ID
     */
    private Long componentId;
    
    /**
     * 指标名称
     */
    private String metricName;
    
    /**
     * 指标标签
     */
    private Map<String, String> tags;
    
    /**
     * 数据值
     */
    private Double value;
    
    /**
     * 时间戳
     */
    private Instant timestamp;
    
    /**
     * 额外数据
     */
    private Map<String, Object> extras;
} 