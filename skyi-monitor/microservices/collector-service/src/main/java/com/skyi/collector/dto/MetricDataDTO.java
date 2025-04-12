package com.skyi.collector.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 指标数据传输对象
 */
@Data
public class MetricDataDTO {
    /**
     * 数据ID
     */
    private Long id;
    
    /**
     * 任务ID
     */
    @NotNull(message = "任务ID不能为空")
    private Long taskId;
    
    /**
     * 任务实例ID
     */
    @NotNull(message = "任务实例ID不能为空")
    private Long instanceId;
    
    /**
     * 资产ID
     */
    @NotNull(message = "资产ID不能为空")
    private Long assetId;
    
    /**
     * 指标名称
     */
    @NotBlank(message = "指标名称不能为空")
    private String metricName;
    
    /**
     * 指标标签
     */
    private Map<String, String> metricLabels;
    
    /**
     * 指标值
     */
    @NotNull(message = "指标值不能为空")
    private Double metricValue;
    
    /**
     * 采集时间
     */
    @NotNull(message = "采集时间不能为空")
    private LocalDateTime collectTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 