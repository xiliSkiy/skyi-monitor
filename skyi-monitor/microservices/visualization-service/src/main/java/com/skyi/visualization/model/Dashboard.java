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
import java.util.List;

/**
 * 仪表盘实体类
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_dashboard")
public class Dashboard {

    /**
     * 仪表盘ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 仪表盘名称
     */
    @Column(length = 100, nullable = false)
    private String name;

    /**
     * 仪表盘标题
     */
    @Column(length = 200)
    private String title;

    /**
     * 仪表盘描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 仪表盘布局配置，JSON格式
     */
    @Column(columnDefinition = "TEXT")
    private String layout;

    /**
     * 组件列表，关联DashboardComponent
     */
    @JsonIgnoreProperties("dashboard")
    @OneToMany(mappedBy = "dashboard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DashboardComponent> components;

    /**
     * 是否为默认仪表盘
     */
    private Boolean isDefault;

    /**
     * 是否共享（所有用户可见）
     */
    private Boolean isShared;

    /**
     * 所有者ID
     */
    private Long ownerId;

    /**
     * 所有者名称
     */
    @Column(length = 100)
    private String ownerName;

    /**
     * 自动刷新间隔（秒）
     */
    private Integer refreshInterval;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    /**
     *
     * 更新时间
     */
    @UpdateTimestamp
    private LocalDateTime updateTime;
} 