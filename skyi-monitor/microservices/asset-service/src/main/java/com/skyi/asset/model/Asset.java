package com.skyi.asset.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 资产实体类
 */
@Data
@Entity
@Table(name = "t_asset")
public class Asset {
    /**
     * 资产ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 资产名称
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * 资产编码
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    
    /**
     * 资产类型（server-服务器，database-数据库，middleware-中间件，application-应用）
     */
    @Column(nullable = false, length = 20)
    private String type;
    
    /**
     * IP地址
     */
    @Column(length = 50)
    private String ipAddress;
    
    /**
     * 端口
     */
    private Integer port;
    
    /**
     * 所属部门
     */
    @Column(length = 50)
    private String department;
    
    /**
     * 负责人
     */
    @Column(length = 50)
    private String owner;
    
    /**
     * 状态（1-正常，0-禁用）
     */
    @Column(nullable = false)
    private Integer status = 1;
    
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