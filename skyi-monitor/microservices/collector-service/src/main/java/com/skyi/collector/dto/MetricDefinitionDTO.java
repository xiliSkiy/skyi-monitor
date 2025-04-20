package com.skyi.collector.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 指标定义数据传输对象
 */
@Data
public class MetricDefinitionDTO {
    /**
     * 指标ID
     */
    private Long id;
    
    /**
     * 指标唯一编码
     */
    @NotBlank(message = "指标编码不能为空")
    private String code;
    
    /**
     * 指标名称
     */
    @NotBlank(message = "指标名称不能为空")
    private String name;
    
    /**
     * 指标描述
     */
    private String description;
    
    /**
     * 指标类别
     */
    @NotBlank(message = "指标类别不能为空")
    private String category;
    
    /**
     * 数据类型
     */
    @NotBlank(message = "数据类型不能为空")
    private String dataType;
    
    /**
     * 单位
     */
    private String unit;
    
    /**
     * 默认值
     */
    private Double defaultValue;
    
    /**
     * 最小阈值
     */
    private Double thresholdMin;
    
    /**
     * 最大阈值
     */
    private Double thresholdMax;
    
    /**
     * 采集方式
     */
    @NotBlank(message = "采集方式不能为空")
    private String collectionMethod;
    
    /**
     * 采集参数模板
     */
    private String paramTemplate;
    
    /**
     * 状态
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
    
    /**
     * 协议映射列表
     */
    @Valid
    private List<MetricProtocolMappingDTO> protocolMappings;
    
    /**
     * 资产类型映射列表
     */
    @Valid
    private List<MetricAssetTypeMappingDTO> assetTypeMappings;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新人
     */
    private String updatedBy;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
} 