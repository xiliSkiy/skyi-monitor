package com.skyi.collector.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 采集任务调度配置实体类
 * 用于存储用户在界面上配置的调度参数
 */
@Data
@Entity
@Table(name = "t_collector_task_schedule")
public class CollectorTaskSchedule {
    /**
     * 调度ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 调度名称
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * 任务ID
     */
    @Column(nullable = false)
    private Long taskId;
    
    /**
     * 调度类型
     * 1-固定频率(fixedRate)
     * 2-CRON表达式
     * 3-一次性任务
     */
    @Column(nullable = false)
    private Integer scheduleType;
    
    /**
     * 执行频率(秒)，当scheduleType=1时有效
     */
    private Integer fixedRate;
    
    /**
     * CRON表达式，当scheduleType=2时有效
     */
    @Column(length = 100)
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
    @Column(nullable = false)
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
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updateTime;
} 