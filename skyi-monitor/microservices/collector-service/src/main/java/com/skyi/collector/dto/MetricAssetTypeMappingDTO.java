package com.skyi.collector.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 指标资产类型映射数据传输对象
 */
@Data
public class MetricAssetTypeMappingDTO {
    /**
     * 映射ID
     */
    private Long id;
    
    /**
     * 指标定义ID
     */
    private Long metricId;
    
    /**
     * 资产类型
     */
    @NotBlank(message = "资产类型不能为空")
    private String assetType;
    
    /**
     * 默认启用标志
     */
    @NotNull(message = "默认启用标志不能为空")
    private Integer defaultEnabled = 1;
} 