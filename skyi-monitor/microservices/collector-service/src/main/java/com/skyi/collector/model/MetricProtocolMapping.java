package com.skyi.collector.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 指标协议映射实体类
 */
@Data
@Entity
@Table(name = "t_metric_protocol_mapping")
public class MetricProtocolMapping {
    /**
     * 映射ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 指标定义ID
     */
    @Column(nullable = false)
    private Long metricId;
    
    /**
     * 协议类型
     */
    @Column(nullable = false, length = 50)
    private String protocol;
    
    /**
     * 指标路径(如SNMP的OID)
     */
    @Column(nullable = false, length = 255)
    private String path;
    
    /**
     * 解析表达式(支持脚本或公式)
     */
    @Column(columnDefinition = "TEXT")
    private String expression;
    
    /**
     * 额外参数(JSON格式)
     */
    @Column(columnDefinition = "TEXT")
    private String parameters;
    
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