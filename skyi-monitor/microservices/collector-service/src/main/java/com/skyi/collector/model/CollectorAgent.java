package com.skyi.collector.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 采集Agent实体类
 */
@Data
@Entity
@Table(name = "t_collector_agent")
public class CollectorAgent {
    /**
     * Agent ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Agent名称
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * Agent编码
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    
    /**
     * IP地址
     */
    @Column(length = 50)
    private String ipAddress;
    
    /**
     * 版本
     */
    @Column(length = 20)
    private String version;
    
    /**
     * 状态（1-在线，0-离线）
     */
    @Column(nullable = false)
    private Integer status = 0;
    
    /**
     * 支持的协议类型，多个协议以逗号分隔
     */
    @Column(length = 200)
    private String supportedProtocols;
    
    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeatTime;
    
    /**
     * 描述
     */
    @Column(length = 500)
    private String description;
    
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