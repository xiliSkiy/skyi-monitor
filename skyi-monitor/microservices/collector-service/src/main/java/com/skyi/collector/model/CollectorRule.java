package com.skyi.collector.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 调度规则实体类
 */
@Data
@Entity
@Table(name = "t_collector_rule")
public class CollectorRule {
    /**
     * 规则ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 规则名称
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * 规则类型（cron, interval, fixedTime）
     */
    @Column(nullable = false, length = 20)
    private String type;
    
    /**
     * 表达式
     * cron类型: cron表达式，如 0 0 12 * * ?
     * interval类型: 间隔秒数，如 60
     * fixedTime类型: 固定时间，如 12:00:00
     */
    @Column(nullable = false, length = 100)
    private String expression;
    
    /**
     * 状态（1-启用，0-禁用）
     */
    @Column(nullable = false)
    private Integer status = 0;
    
    /**
     * 描述
     */
    @Column(length = 500)
    private String description;
    
    /**
     * 关联的任务ID列表，JSON字符串
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String taskIds;
    
    /**
     * 执行超时时间(秒)
     */
    @Column(nullable = false)
    private Integer timeout = 60;
    
    /**
     * 失败重试次数
     */
    @Column(nullable = false)
    private Integer retryCount = 0;
    
    /**
     * 重试间隔(秒)
     */
    @Column(nullable = false)
    private Integer retryInterval = 30;
    
    /**
     * 上次执行时间
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