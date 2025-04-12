package com.skyi.collector.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

/**
 * 采集任务调度配置DTO
 */
@Data
public class CollectorTaskScheduleDTO {
    /**
     * 调度ID
     */
    private Long id;
    
    /**
     * 调度名称
     */
    @NotBlank(message = "调度名称不能为空")
    private String name;
    
    /**
     * 任务ID
     */
    @NotNull(message = "任务ID不能为空")
    private Long taskId;
    
    /**
     * 任务名称（显示用）
     */
    private String taskName;
    
    /**
     * 调度类型
     * 1-固定频率(fixedRate)
     * 2-CRON表达式
     * 3-一次性任务
     */
    @NotNull(message = "调度类型不能为空")
    private Integer scheduleType;
    
    /**
     * 执行频率(秒)，当scheduleType=1时有效
     */
    @Min(value = 10, message = "执行频率不能小于10秒")
    private Integer fixedRate;
    
    /**
     * CRON表达式，当scheduleType=2时有效
     */
    private String cronExpression;
    
    /**
     * 执行时间，当scheduleType=3时有效
     */
    private LocalDateTime executeTime;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetries;
    
    /**
     * 重试间隔(秒)
     */
    private Integer retryInterval;
    
    /**
     * 是否启用
     * 1-启用，0-禁用
     */
    @NotNull(message = "是否启用不能为空")
    private Integer enabled = 1;
    
    /**
     * 最后执行时间
     */
    private LocalDateTime lastExecuteTime;
    
    /**
     * 下次执行时间
     */
    private LocalDateTime nextExecuteTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 