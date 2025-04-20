package com.skyi.alert.dto;

import com.skyi.alert.model.AlertSeverity;
import com.skyi.alert.model.AlertStatus;
import com.skyi.alert.model.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 告警数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDTO {
    private Long id;
    private String alertUuid;
    private String name;
    private String message;
    private AlertSeverity severity;
    private AlertStatus status;
    private AlertType type;
    private Long assetId;
    private String assetName;
    private String assetType;
    private String metricName;
    private Double metricValue;
    private Double threshold;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime acknowledgedTime;
    private String acknowledgedBy;
    private LocalDateTime resolvedTime;
    private String resolvedBy;
    private String resolveComment;
    private Boolean notified;
    private LocalDateTime lastNotifiedTime;
    private Integer notificationCount;
    private String tags;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 