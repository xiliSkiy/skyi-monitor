package com.skyi.collector.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 指标协议映射数据传输对象
 */
@Data
public class MetricProtocolMappingDTO {
    /**
     * 映射ID
     */
    private Long id;
    
    /**
     * 指标定义ID
     */
    private Long metricId;
    
    /**
     * 协议
     */
    @NotBlank(message = "协议不能为空")
    private String protocol;
    
    /**
     * 指标路径
     */
    @NotBlank(message = "指标路径不能为空")
    private String path;
    
    /**
     * 解析表达式
     */
    private String expression;
    
    /**
     * 额外参数
     */
    private String parameters;
} 