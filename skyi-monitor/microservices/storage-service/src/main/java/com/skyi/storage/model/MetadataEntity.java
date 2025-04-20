package com.skyi.storage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 元数据实体
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "metadata")
public class MetadataEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 元数据类型
     */
    @Column(nullable = false, length = 50)
    private String type;
    
    /**
     * 关联ID
     */
    @Column(nullable = false, length = 100)
    private String refId;
    
    /**
     * 元数据键
     */
    @Column(nullable = false, length = 100)
    private String metaKey;
    
    /**
     * 元数据值
     */
    @Column(columnDefinition = "TEXT")
    private String metaValue;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @UpdateTimestamp
    private LocalDateTime updateTime;
} 