package com.skyi.processor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 采集状态信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionStatus {
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 任务实例ID
     */
    private Long instanceId;
    
    /**
     * 资产ID
     */
    private Long assetId;
    
    /**
     * 开始时间
     */
    private Instant startTime;
    
    /**
     * 结束时间
     */
    private Instant endTime;
    
    /**
     * 状态
     */
    private Status status;
    
    /**
     * 数据点数量
     */
    private Integer dataPointCount;
    
    /**
     * 错误消息
     */
    private String errorMessage;
    
    /**
     * 采集时长(毫秒)
     */
    private Long duration;
    
    /**
     * 状态枚举
     */
    public enum Status {
        /**
         * 开始
         */
        STARTED,
        
        /**
         * 成功
         */
        SUCCESS,
        
        /**
         * 失败
         */
        FAILED,
        
        /**
         * 部分成功
         */
        PARTIAL_SUCCESS,
        
        /**
         * 超时
         */
        TIMEOUT
    }
} 