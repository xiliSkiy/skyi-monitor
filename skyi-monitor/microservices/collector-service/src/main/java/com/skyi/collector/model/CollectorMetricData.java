package com.skyi.collector.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 采集指标数据实体类
 */
@Data
@Entity
@Table(name = "t_collector_metric_data")
public class CollectorMetricData {
    /**
     * 数据ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 任务ID
     */
    @Column(nullable = false)
    private Long taskId;
    
    /**
     * 任务实例ID
     */
    @Column(nullable = false)
    private Long instanceId;
    
    /**
     * 资产ID
     */
    @Column(nullable = false)
    private Long assetId;
    
    /**
     * 指标名称
     */
    @Column(nullable = false, length = 100)
    private String metricName;
    
    /**
     * 指标标签，JSON字符串
     */
    @Column(columnDefinition = "TEXT")
    private String metricLabels;
    
    /**
     * 指标值
     */
    @Column(nullable = false)
    private Double metricValue;
    
    /**
     * 采集时间
     */
    @Column(nullable = false)
    private LocalDateTime collectTime;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;
} 