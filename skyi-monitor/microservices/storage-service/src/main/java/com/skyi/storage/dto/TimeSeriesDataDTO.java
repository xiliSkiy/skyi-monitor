package com.skyi.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;

/**
 * 时间序列数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesDataDTO {

    /**
     * 时间戳
     */
    private Instant time;
    
    /**
     * 资产ID
     */
    @NotBlank(message = "资产ID不能为空")
    private String assetId;
    
    /**
     * 指标名称
     */
    @NotBlank(message = "指标名称不能为空")
    private String metricName;
    
    /**
     * 指标值
     */
    @NotNull(message = "指标值不能为空")
    private Double value;
    
    /**
     * 单位
     */
    private String unit;
    
    /**
     * 标签（附加信息）
     */
    private Map<String, String> tags;
} 