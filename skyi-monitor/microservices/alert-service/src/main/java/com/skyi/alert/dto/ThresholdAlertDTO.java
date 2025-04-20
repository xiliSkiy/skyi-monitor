package com.skyi.alert.dto;

import com.skyi.alert.model.AlertSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 阈值告警DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdAlertDTO {
    /**
     * 事件类型
     */
    private String eventType;
    
    /**
     * 资产ID
     */
    private Long assetId;
    
    /**
     * 资产名称
     */
    private String assetName;
    
    /**
     * 资产类型
     */
    private String assetType;
    
    /**
     * 指标名称
     */
    private String metricName;
    
    /**
     * 指标值
     */
    private Double value;
    
    /**
     * 阈值
     */
    private Double threshold;
    
    /**
     * 事件时间
     */
    private LocalDateTime timestamp;
    
    /**
     * 持续时间（秒）
     */
    private Integer duration;
    
    /**
     * 告警级别
     */
    private AlertSeverity severity;
    
    /**
     * 额外标签
     */
    private Map<String, Object> tags;
} 