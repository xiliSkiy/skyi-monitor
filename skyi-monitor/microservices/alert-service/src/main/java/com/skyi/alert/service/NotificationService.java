package com.skyi.alert.service;

import com.skyi.alert.dto.AlertNotificationDTO;
import com.skyi.alert.model.Alert;
import com.skyi.alert.model.AlertNotification;
import com.skyi.alert.model.NotificationChannel;

import java.util.List;
import java.util.Map;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 发送告警通知
     *
     * @param alert 告警对象
     * @return 通知列表
     */
    List<AlertNotification> sendAlertNotifications(Alert alert);
    
    /**
     * 发送指定渠道的告警通知
     *
     * @param alert 告警对象
     * @param channel 通知渠道
     * @return 通知对象
     */
    AlertNotification sendAlertNotification(Alert alert, NotificationChannel channel);
    
    /**
     * 处理通知DTO
     *
     * @param notificationDTO 通知DTO
     * @return 处理结果
     */
    boolean processNotification(AlertNotificationDTO notificationDTO);
    
    /**
     * 重试失败的通知
     *
     * @return 重试的通知数量
     */
    int retryFailedNotifications();
    
    /**
     * 根据告警ID查询通知
     *
     * @param alertId 告警ID
     * @return 通知列表
     */
    List<AlertNotification> getNotificationsByAlertId(Long alertId);
    
    /**
     * 根据告警UUID查询通知
     *
     * @param alertUuid 告警UUID
     * @return 通知列表
     */
    List<AlertNotification> getNotificationsByAlertUuid(String alertUuid);
    
    /**
     * 获取通知渠道配置
     *
     * @return 渠道配置
     */
    Map<NotificationChannel, Boolean> getNotificationChannelConfig();
    
    /**
     * 按渠道统计通知状态
     *
     * @return 统计结果
     */
    Map<NotificationChannel, Map<String, Long>> countNotificationsByChannelAndStatus();
} 