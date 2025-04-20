package com.skyi.visualization.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 仪表盘组件实体类
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_dashboard_component")
public class DashboardComponent {

    /**
     * 组件ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属仪表盘
     */
    @JsonIgnoreProperties("components") 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dashboard_id", nullable = false)
    private Dashboard dashboard;

    /**
     * 组件类型：LINE_CHART（折线图）、BAR_CHART（柱状图）、PIE_CHART（饼图）、
     * GAUGE（仪表盘）、TABLE（表格）、VALUE（单值）、TEXT（文本）
     */
    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private ComponentType type;

    /**
     * 组件标题
     */
    @Column(length = 200)
    private String title;

    /**
     * 组件描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 组件位置信息，JSON格式，包含x, y, w, h
     */
    @Column(columnDefinition = "JSON")
    private String position;

    /**
     * 数据源配置，JSON格式
     */
    @Column(columnDefinition = "TEXT")
    private String dataSource;

    /**
     * 样式配置，JSON格式
     */
    @Column(columnDefinition = "TEXT")
    private String styles;

    /**
     * 是否支持实时数据
     */
    private Boolean realtime;

    /**
     * 刷新间隔（秒）
     */
    private Integer refreshInterval;

    /**
     * 时间范围（秒）
     */
    private Integer timeRange;

    /**
     * 排序号
     */
    private Integer sortOrder;

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