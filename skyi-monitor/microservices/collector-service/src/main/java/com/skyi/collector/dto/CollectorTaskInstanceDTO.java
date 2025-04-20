package com.skyi.collector.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 采集任务实例数据传输对象
 */
@Data
public class CollectorTaskInstanceDTO {
    /**
     * 实例ID
     */
    private Long id;
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 任务类型
     */
    private String taskType;
    
    /**
     * 资产ID
     */
    private Long assetId;
    
    /**
     * 资产名称
     */
    private String assetName;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 执行状态（0-失败，1-成功，2-进行中）
     */
    private Integer status;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 总采集指标数量
     */
    private Integer metricCount;
    
    /**
     * 执行时长(毫秒)
     */
    private Long duration;
} 