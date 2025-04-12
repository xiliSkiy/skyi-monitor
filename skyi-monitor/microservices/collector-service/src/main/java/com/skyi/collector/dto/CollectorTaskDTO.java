package com.skyi.collector.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 采集任务数据传输对象
 */
@Data
public class CollectorTaskDTO {
    /**
     * 任务ID
     */
    private Long id;
    
    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    private String name;
    
    /**
     * 任务编码
     */
    @NotBlank(message = "任务编码不能为空")
    private String code;
    
    /**
     * 任务类型
     */
    @NotBlank(message = "任务类型不能为空")
    @Pattern(regexp = "server|database|middleware|application", message = "任务类型只能是server/database/middleware/application")
    private String type;
    
    /**
     * 协议类型
     */
    @NotBlank(message = "协议类型不能为空")
    private String protocol;
    
    /**
     * 资产ID
     */
    @NotNull(message = "资产ID不能为空")
    private Long assetId;
    
    /**
     * 资产名称
     */
    private String assetName;
    
    /**
     * 采集间隔(秒)
     */
    @NotNull(message = "采集间隔不能为空")
    @Min(value = 10, message = "采集间隔不能小于10秒")
    private Integer interval;
    
    /**
     * 采集指标列表
     */
    @NotNull(message = "采集指标不能为空")
    private List<MetricDTO> metrics;
    
    /**
     * 连接参数
     */
    private Map<String, String> connectionParams;
    
    /**
     * 状态
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 最后执行时间
     */
    private LocalDateTime lastExecuteTime;
    
    /**
     * 最后执行状态
     */
    private Integer lastExecuteStatus;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 指标数据传输对象
     */
    @Data
    public static class MetricDTO {
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
         * 采集路径/OID/SQL
         */
        @NotBlank(message = "采集路径不能为空")
        private String path;
        
        /**
         * 数据类型（gauge,counter等）
         */
        @NotBlank(message = "数据类型不能为空")
        private String dataType;
        
        /**
         * 单位
         */
        private String unit;
        
        /**
         * 是否启用
         */
        private Boolean enabled = true;
    }
} 