package com.skyi.collector.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 指标资产类型映射实体类
 */
@Data
@Entity
@Table(name = "t_metric_asset_type_mapping")
public class MetricAssetTypeMapping {
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
     * 资产类型
     */
    @Column(nullable = false, length = 50)
    private String assetType;
    
    /**
     * 默认启用标志
     */
    @Column(nullable = false)
    private Integer defaultEnabled = 1;
    
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