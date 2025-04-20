package com.skyi.collector.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 采集任务实体类
 */
@Data
@Entity
@Table(name = "t_collector_task")
public class CollectorTask {
    /**
     * 任务ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 任务名称
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * 任务编码
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    
    /**
     * 任务类型（server-服务器，database-数据库，middleware-中间件，application-应用）
     */
    @Column(nullable = false, length = 20)
    private String type;
    
    /**
     * 协议类型（snmp,ssh,jdbc,http,jmx,prometheus等）
     */
    @Column(nullable = false, length = 20)
    private String protocol;
    
    /**
     * 资产ID
     */
    @Column(nullable = false)
    private Long assetId;
    
    /**
     * 采集间隔(秒)
     */
    @Column(name = "`interval`", nullable = false)
    private Integer interval;
    
    /**
     * 采集指标列表，JSON字符串
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String metrics;
    
    /**
     * 连接参数，JSON字符串
     */
    @Column(columnDefinition = "TEXT")
    private String connectionParams;
    
    /**
     * 状态（1-启用，0-禁用）
     */
    @Column(nullable = false)
    private Integer status = 1;
    
    /**
     * 描述
     */
    @Column(length = 500)
    private String description;
    
    /**
     * 最后执行时间
     */
    private LocalDateTime lastExecuteTime;
    
    /**
     * 最后执行状态（1-成功，0-失败）
     */
    private Integer lastExecuteStatus;
    
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