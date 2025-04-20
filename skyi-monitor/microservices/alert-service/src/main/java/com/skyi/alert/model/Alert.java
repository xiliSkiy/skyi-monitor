package com.skyi.alert.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 告警实体类
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_alert")
public class Alert {

    /**
     * 告警ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 告警UUID
     */
    @Column(length = 64, unique = true, nullable = false)
    private String alertUuid;

    /**
     * 告警名称
     */
    @Column(length = 200, nullable = false)
    private String name;

    /**
     * 告警内容
     */
    @Column(length = 1000)
    private String message;

    /**
     * 告警级别：CRITICAL（严重）、WARNING（警告）、INFO（提示）
     */
    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertSeverity severity;

    /**
     * 告警状态：ACTIVE（未处理）、ACKNOWLEDGED（已确认）、RESOLVED（已解决）、CLOSED（已关闭）
     */
    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertStatus status;

    /**
     * 告警类型：THRESHOLD（阈值）、TREND（趋势）、PATTERN（模式）
     */
    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertType type;

    /**
     * 资产ID
     */
    @Column(nullable = false)
    private Long assetId;

    /**
     * 资产名称
     */
    @Column(length = 200)
    private String assetName;

    /**
     * 资产类型
     */
    @Column(length = 100)
    private String assetType;

    /**
     * 指标名称
     */
    @Column(length = 100)
    private String metricName;

    /**
     * 指标值
     */
    private Double metricValue;

    /**
     * 阈值
     */
    private Double threshold;

    /**
     * 告警开始时间
     */
    @Column(nullable = false)
    private LocalDateTime startTime;

    /**
     * 告警结束时间
     */
    private LocalDateTime endTime;

    /**
     * 告警确认时间
     */
    private LocalDateTime acknowledgedTime;

    /**
     * 告警确认人
     */
    @Column(length = 100)
    private String acknowledgedBy;

    /**
     * 告警解决时间
     */
    private LocalDateTime resolvedTime;

    /**
     * 告警解决人
     */
    @Column(length = 100)
    private String resolvedBy;

    /**
     * 告警处理备注
     */
    @Column(length = 1000)
    private String resolveComment;

    /**
     * 是否已通知
     */
    private Boolean notified;

    /**
     * 上次通知时间
     */
    private LocalDateTime lastNotifiedTime;

    /**
     * 通知次数
     */
    private Integer notificationCount;

    /**
     * 告警标签，JSON格式
     */
    @Column(columnDefinition = "TEXT")
    private String tags;

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