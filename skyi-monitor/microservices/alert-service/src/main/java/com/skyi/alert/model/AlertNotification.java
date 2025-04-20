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
 * 告警通知实体类
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_alert_notification")
public class AlertNotification {

    /**
     * 通知ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 告警ID
     */
    @Column(nullable = false)
    private Long alertId;
    
    /**
     * 告警UUID
     */
    @Column(length = 64, nullable = false)
    private String alertUuid;
    
    /**
     * 通知渠道：EMAIL、SMS、WEBHOOK、DINGTALK、WEIXIN
     */
    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;
    
    /**
     * 接收人
     */
    @Column(length = 200)
    private String recipient;
    
    /**
     * 通知内容
     */
    @Column(columnDefinition = "TEXT")
    private String content;
    
    /**
     * 通知状态：PENDING、SUCCESS、FAILED
     */
    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
    
    /**
     * 失败原因
     */
    @Column(length = 500)
    private String failReason;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 最后发送时间
     */
    private LocalDateTime sentTime;
    
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