package com.skyi.asset.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 资产标签实体类
 */
@Data
@Entity
@Table(name = "t_asset_tag")
public class AssetTag {
    /**
     * 标签ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 资产ID
     */
    @Column(nullable = false)
    private Long assetId;
    
    /**
     * 标签键
     */
    @Column(nullable = false, length = 50)
    private String tagKey;
    
    /**
     * 标签值
     */
    @Column(nullable = false, length = 100)
    private String tagValue;
    
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