package com.skyi.collector.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 采集任务实例实体类
 */
@Data
@Entity
@Table(name = "t_collector_task_instance")
public class CollectorTaskInstance {
    /**
     * 实例ID
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
     * 关联的任务
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taskId", referencedColumnName = "id", insertable = false, updatable = false)
    private CollectorTask task;
    
    /**
     * 任务调度ID
     */
    private Long scheduleId;
    
    /**
     * 资产ID
     */
    @Column(nullable = false)
    private Long assetId;
    
    /**
     * 开始时间
     */
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 状态（1-成功，0-失败，2-进行中）
     */
    @Column(nullable = false)
    private Integer status;
    
    /**
     * 错误消息
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 采集数据点数量
     */
    private Integer dataPointCount;
    
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
    
    /**
     * 获取关联的任务对象
     * 
     * @return 任务对象
     */
    public CollectorTask getTask() {
        return task;
    }
    
    /**
     * 设置关联的任务对象
     * 
     * @param task 任务对象
     */
    public void setTask(CollectorTask task) {
        this.task = task;
        if (task != null) {
            this.taskId = task.getId();
        }
    }
} 