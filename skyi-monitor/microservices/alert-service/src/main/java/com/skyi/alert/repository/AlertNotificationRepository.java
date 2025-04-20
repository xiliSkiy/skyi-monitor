package com.skyi.alert.repository;

import com.skyi.alert.model.AlertNotification;
import com.skyi.alert.model.NotificationChannel;
import com.skyi.alert.model.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警通知仓库接口
 */
public interface AlertNotificationRepository extends JpaRepository<AlertNotification, Long> {

    /**
     * 根据告警ID查询通知
     */
    List<AlertNotification> findByAlertId(Long alertId);
    
    /**
     * 根据告警UUID查询通知
     */
    List<AlertNotification> findByAlertUuid(String alertUuid);
    
    /**
     * 根据通知状态查询
     */
    List<AlertNotification> findByStatus(NotificationStatus status);
    
    /**
     * 根据通知状态和渠道查询
     */
    List<AlertNotification> findByStatusAndChannel(NotificationStatus status, NotificationChannel channel);
    
    /**
     * 查询需要重试的失败通知
     */
    @Query("SELECT n FROM AlertNotification n WHERE n.status = 'FAILED' AND n.retryCount < :maxRetries AND n.createTime > :timeThreshold")
    List<AlertNotification> findFailedNotificationsForRetry(@Param("maxRetries") Integer maxRetries, @Param("timeThreshold") LocalDateTime timeThreshold);
    
    /**
     * 分页查询告警通知
     */
    Page<AlertNotification> findByAlertId(Long alertId, Pageable pageable);
    
    /**
     * 按渠道统计通知状态
     */
    @Query("SELECT n.channel, n.status, COUNT(n) FROM AlertNotification n GROUP BY n.channel, n.status")
    List<Object[]> countNotificationsByChannelAndStatus();
} 