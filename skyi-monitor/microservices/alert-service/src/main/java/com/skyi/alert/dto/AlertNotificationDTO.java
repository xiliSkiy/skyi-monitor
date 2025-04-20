package com.skyi.alert.dto;

import com.skyi.alert.model.AlertSeverity;
import com.skyi.alert.model.NotificationChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 告警通知DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertNotificationDTO {
    /**
     * 告警ID
     */
    private Long alertId;
    
    /**
     * 告警UUID
     */
    private String alertUuid;
    
    /**
     * 告警名称
     */
    private String alertName;
    
    /**
     * 告警消息
     */
    private String alertMessage;
    
    /**
     * 告警级别
     */
    private AlertSeverity severity;
    
    /**
     * 资产ID
     */
    private Long assetId;
    
    /**
     * 资产名称
     */
    private String assetName;
    
    /**
     * 资产类型
     */
    private String assetType;
    
    /**
     * 指标名称
     */
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
     * 告警时间
     */
    private LocalDateTime alertTime;
    
    /**
     * 通知渠道
     */
    private List<NotificationChannel> channels;
    
    /**
     * 通知接收人
     */
    private List<String> recipients;
    
    /**
     * 额外信息
     */
    private Map<String, Object> extraInfo;
} 