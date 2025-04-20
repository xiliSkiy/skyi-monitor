package com.skyi.visualization.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 报表实体类
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_report")
public class Report {

    /**
     * 报表ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 报表名称
     */
    @Column(length = 100, nullable = false)
    private String name;

    /**
     * 报表标题
     */
    @Column(length = 200)
    private String title;

    /**
     * 报表描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 报表类型：DAILY（日报）、WEEKLY（周报）、MONTHLY（月报）、CUSTOM（自定义）
     */
    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportType type;

    /**
     * 报表模板内容，JSON格式
     */
    @Column(columnDefinition = "TEXT")
    private String template;

    /**
     * 报表参数配置，JSON格式
     */
    @Column(columnDefinition = "TEXT")
    private String parameters;

    /**
     * 报表计划配置，JSON格式，包含定时任务规则
     */
    @Column(columnDefinition = "TEXT")
    private String schedule;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 上次生成时间
     */
    private LocalDateTime lastGeneratedTime;

    /**
     * 上次生成状态：SUCCESS、FAILED
     */
    @Column(length = 20)
    private String lastGeneratedStatus;

    /**
     * 生成的报表存储路径
     */
    @Column(length = 500)
    private String reportPath;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 创建者名称
     */
    @Column(length = 100)
    private String creatorName;

    /**
     * 接收人列表，JSON格式
     */
    @Column(columnDefinition = "TEXT")
    private String recipients;

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