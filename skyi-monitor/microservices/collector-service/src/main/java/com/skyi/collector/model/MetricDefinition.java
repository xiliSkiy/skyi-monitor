package com.skyi.collector.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 指标定义实体类
 */
@Data
@Entity
@Table(name = "t_metric_definition")
public class MetricDefinition {
    /**
     * 指标定义ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 指标唯一编码
     */
    @Column(nullable = false, length = 100, unique = true)
    private String code;
    
    /**
     * 指标名称
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * 指标描述
     */
    @Column(length = 500)
    private String description;
    
    /**
     * 指标类别(系统/网络/应用等)
     */
    @Column(nullable = false, length = 50)
    private String category;
    
    /**
     * 数据类型(gauge/counter/histogram等)
     */
    @Column(nullable = false, length = 20)
    private String dataType;
    
    /**
     * 单位(百分比/字节/秒等)
     */
    @Column(length = 20)
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
     * 采集方式(snmp/ssh/http等)
     */
    @Column(nullable = false, length = 50)
    private String collectionMethod;
    
    /**
     * 采集参数模板(JSON格式)
     */
    @Column(columnDefinition = "TEXT")
    private String paramTemplate;
    
    /**
     * 状态(1-启用,0-禁用)
     */
    @Column(nullable = false)
    private Integer status = 1;
    
    /**
     * 创建人
     */
    @Column(length = 50)
    private String createdBy;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;
    
    /**
     * 更新人
     */
    @Column(length = 50)
    private String updatedBy;
    
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedTime;
} 