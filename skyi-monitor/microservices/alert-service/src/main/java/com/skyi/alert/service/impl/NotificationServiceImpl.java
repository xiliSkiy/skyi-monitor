package com.skyi.alert.service.impl;

import com.skyi.alert.constant.KafkaTopicConstants;
import com.skyi.alert.dto.AlertNotificationDTO;
import com.skyi.alert.model.Alert;
import com.skyi.alert.model.AlertNotification;
import com.skyi.alert.model.NotificationChannel;
import com.skyi.alert.model.NotificationStatus;
import com.skyi.alert.repository.AlertNotificationRepository;
import com.skyi.alert.service.MessageService;
import com.skyi.alert.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通知服务实现类
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private MessageService messageService;
    
    @Autowired
    private AlertNotificationRepository alertNotificationRepository;
    
    // 是否启用邮件通知
    @Value("${skyi.alert.notification.email.enabled:false}")
    private boolean emailEnabled;
    
    // 是否启用短信通知
    @Value("${skyi.alert.notification.sms.enabled:false}")
    private boolean smsEnabled;
    
    // 是否启用Webhook通知
    @Value("${skyi.alert.notification.webhook.enabled:false}")
    private boolean webhookEnabled;
    
    // 是否启用钉钉通知
    @Value("${skyi.alert.notification.dingtalk.enabled:false}")
    private boolean dingtalkEnabled;
    
    // 是否启用企业微信通知
    @Value("${skyi.alert.notification.weixin.enabled:false}")
    private boolean weixinEnabled;
    
    // 最大重试次数
    @Value("${skyi.alert.notification.max-retries:3}")
    private int maxRetries;

    @Override
    @Transactional
    public List<AlertNotification> sendAlertNotifications(Alert alert) {
        log.info("开始为告警发送通知: alertId={}, 级别={}", alert.getId(), alert.getSeverity());
        
        List<AlertNotification> notifications = new ArrayList<>();
        
        // 根据配置决定使用哪些通知渠道
        List<NotificationChannel> enabledChannels = getEnabledChannels();
        if (enabledChannels.isEmpty()) {
            log.warn("未配置任何可用的通知渠道，告警通知将被忽略");
            return notifications;
        }
        
        for (NotificationChannel channel : enabledChannels) {
            try {
                AlertNotification notification = sendAlertNotification(alert, channel);
                if (notification != null) {
                    notifications.add(notification);
                }
            } catch (Exception e) {
                log.error("通过渠道[{}]发送告警通知失败: {}", channel, e.getMessage());
            }
        }
        
        return notifications;
    }

    @Override
    @Transactional
    public AlertNotification sendAlertNotification(Alert alert, NotificationChannel channel) {
        log.info("通过渠道[{}]发送告警通知: alertId={}", channel, alert.getId());
        
        // 构造通知数据
        AlertNotificationDTO notificationDTO = buildNotificationDTO(alert, channel);
        
        // 创建通知记录
        AlertNotification notification = AlertNotification.builder()
                .alertId(alert.getId())
                .alertUuid(alert.getAlertUuid())
                .channel(channel)
                .content(generateNotificationContent(alert, channel))
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .build();
        
        // 设置接收人（实际应该从配置或用户管理系统获取）
        notification.setRecipient(getRecipientByChannel(channel));
        
        // 保存通知记录
        notification = alertNotificationRepository.save(notification);
        
        // 发送消息到Kafka，由其他服务处理实际的通知发送
        boolean sent = messageService.sendMessage(
                KafkaTopicConstants.TOPIC_ALERT_NOTIFICATION,
                alert.getAlertUuid(),
                notificationDTO);
        
        // 更新通知状态
        if (sent) {
            notification.setSentTime(LocalDateTime.now());
            notification.setStatus(NotificationStatus.PENDING);
            log.info("通知消息已发送到队列: notificationId={}, channel={}", notification.getId(), channel);
        } else {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailReason("无法发送消息到队列");
            log.error("无法发送通知消息到队列: notificationId={}, channel={}", notification.getId(), channel);
        }
        
        return alertNotificationRepository.save(notification);
    }
    
    @Override
    @Transactional
    public boolean processNotification(AlertNotificationDTO notificationDTO) {
        log.info("处理告警通知: alertId={}, alertUuid={}, channels={}",
                notificationDTO.getAlertId(), notificationDTO.getAlertUuid(),
                notificationDTO.getChannels());
        
        try {
            // 根据告警UUID查找现有通知
            List<AlertNotification> notifications = alertNotificationRepository
                    .findByAlertUuid(notificationDTO.getAlertUuid());
            
            // 如果找不到通知记录，说明可能是直接来自其他服务的消息，我们创建新的通知记录
            if (notifications.isEmpty()) {
                List<NotificationChannel> channels = notificationDTO.getChannels();
                if (channels == null || channels.isEmpty()) {
                    channels = getEnabledChannels();
                }
                
                for (NotificationChannel channel : channels) {
                    AlertNotification notification = AlertNotification.builder()
                            .alertId(notificationDTO.getAlertId())
                            .alertUuid(notificationDTO.getAlertUuid())
                            .channel(channel)
                            .recipient(getRecipientFromDTO(notificationDTO, channel))
                            .content(generateContentFromDTO(notificationDTO, channel))
                            .status(NotificationStatus.SUCCESS) // 假设处理成功
                            .sentTime(LocalDateTime.now())
                            .retryCount(0)
                            .build();
                    
                    alertNotificationRepository.save(notification);
                }
            } else {
                // 更新现有通知记录
                for (AlertNotification notification : notifications) {
                    notification.setStatus(NotificationStatus.SUCCESS);
                    notification.setSentTime(LocalDateTime.now());
                    alertNotificationRepository.save(notification);
                }
            }
            
            return true;
        } catch (Exception e) {
            log.error("处理告警通知失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public int retryFailedNotifications() {
        // 查找需要重试的失败通知
        List<AlertNotification> failedNotifications = alertNotificationRepository
                .findFailedNotificationsForRetry(maxRetries, LocalDateTime.now().minusDays(1));
        
        int retried = 0;
        for (AlertNotification notification : failedNotifications) {
            try {
                // 构造通知DTO
                AlertNotificationDTO notificationDTO = buildNotificationDTOFromNotification(notification);
                
                // 发送消息到Kafka
                boolean sent = messageService.sendMessage(
                        KafkaTopicConstants.TOPIC_ALERT_NOTIFICATION,
                        notification.getAlertUuid(),
                        notificationDTO);
                
                if (sent) {
                    notification.setRetryCount(notification.getRetryCount() + 1);
                    notification.setStatus(NotificationStatus.PENDING);
                    notification.setSentTime(LocalDateTime.now());
                    alertNotificationRepository.save(notification);
                    retried++;
                }
            } catch (Exception e) {
                log.error("重试通知失败: notificationId={}, error={}", notification.getId(), e.getMessage());
            }
        }
        
        log.info("重试失败通知完成，成功重试: {}/{}", retried, failedNotifications.size());
        return retried;
    }
    
    @Override
    public List<AlertNotification> getNotificationsByAlertId(Long alertId) {
        return alertNotificationRepository.findByAlertId(alertId);
    }
    
    @Override
    public List<AlertNotification> getNotificationsByAlertUuid(String alertUuid) {
        return alertNotificationRepository.findByAlertUuid(alertUuid);
    }
    
    @Override
    public Map<NotificationChannel, Boolean> getNotificationChannelConfig() {
        Map<NotificationChannel, Boolean> config = new HashMap<>();
        config.put(NotificationChannel.EMAIL, emailEnabled);
        config.put(NotificationChannel.SMS, smsEnabled);
        config.put(NotificationChannel.WEBHOOK, webhookEnabled);
        config.put(NotificationChannel.DINGTALK, dingtalkEnabled);
        config.put(NotificationChannel.WEIXIN, weixinEnabled);
        return config;
    }
    
    @Override
    public Map<NotificationChannel, Map<String, Long>> countNotificationsByChannelAndStatus() {
        List<Object[]> results = alertNotificationRepository.countNotificationsByChannelAndStatus();
        
        Map<NotificationChannel, Map<String, Long>> stats = new HashMap<>();
        for (Object[] result : results) {
            NotificationChannel channel = (NotificationChannel) result[0];
            NotificationStatus status = (NotificationStatus) result[1];
            Long count = (Long) result[2];
            
            stats.putIfAbsent(channel, new HashMap<>());
            stats.get(channel).put(status.name(), count);
        }
        
        // 确保所有渠道和状态都有统计数据
        for (NotificationChannel channel : NotificationChannel.values()) {
            stats.putIfAbsent(channel, new HashMap<>());
            Map<String, Long> channelStats = stats.get(channel);
            
            for (NotificationStatus status : NotificationStatus.values()) {
                channelStats.putIfAbsent(status.name(), 0L);
            }
        }
        
        return stats;
    }
    
    /**
     * 构建通知DTO
     */
    private AlertNotificationDTO buildNotificationDTO(Alert alert, NotificationChannel channel) {
        return AlertNotificationDTO.builder()
                .alertId(alert.getId())
                .alertUuid(alert.getAlertUuid())
                .alertName(alert.getName())
                .alertMessage(alert.getMessage())
                .severity(alert.getSeverity())
                .assetId(alert.getAssetId())
                .assetName(alert.getAssetName())
                .assetType(alert.getAssetType())
                .metricName(alert.getMetricName())
                .metricValue(alert.getMetricValue())
                .threshold(alert.getThreshold())
                .alertTime(alert.getStartTime())
                .channels(Collections.singletonList(channel))
                .recipients(Collections.singletonList(getRecipientByChannel(channel)))
                .build();
    }
    
    /**
     * 从通知记录构建DTO
     */
    private AlertNotificationDTO buildNotificationDTOFromNotification(AlertNotification notification) {
        return AlertNotificationDTO.builder()
                .alertId(notification.getAlertId())
                .alertUuid(notification.getAlertUuid())
                .channels(Collections.singletonList(notification.getChannel()))
                .recipients(Collections.singletonList(notification.getRecipient()))
                .build();
    }
    
    /**
     * 获取已启用的通知渠道
     */
    private List<NotificationChannel> getEnabledChannels() {
        List<NotificationChannel> enabledChannels = new ArrayList<>();
        if (emailEnabled) enabledChannels.add(NotificationChannel.EMAIL);
        if (smsEnabled) enabledChannels.add(NotificationChannel.SMS);
        if (webhookEnabled) enabledChannels.add(NotificationChannel.WEBHOOK);
        if (dingtalkEnabled) enabledChannels.add(NotificationChannel.DINGTALK);
        if (weixinEnabled) enabledChannels.add(NotificationChannel.WEIXIN);
        return enabledChannels;
    }
    
    /**
     * 根据渠道获取接收人（实际应从配置或用户系统获取）
     */
    private String getRecipientByChannel(NotificationChannel channel) {
        // 在实际应用中，应该从配置或用户管理系统中获取
        switch (channel) {
            case EMAIL:
                return "admin@example.com";
            case SMS:
                return "13800138000";
            case WEBHOOK:
                return "https://webhook.example.com/alert";
            case DINGTALK:
                return "https://oapi.dingtalk.com/robot/send?access_token=xxx";
            case WEIXIN:
                return "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx";
            default:
                return "";
        }
    }
    
    /**
     * 从DTO中获取接收人
     */
    private String getRecipientFromDTO(AlertNotificationDTO dto, NotificationChannel channel) {
        if (dto.getRecipients() != null && !dto.getRecipients().isEmpty()) {
            return dto.getRecipients().get(0);
        }
        return getRecipientByChannel(channel);
    }
    
    /**
     * 生成通知内容
     */
    private String generateNotificationContent(Alert alert, NotificationChannel channel) {
        StringBuilder content = new StringBuilder();
        content.append("【告警通知】").append(alert.getSeverity()).append("\n");
        content.append("告警名称：").append(alert.getName()).append("\n");
        content.append("告警内容：").append(alert.getMessage()).append("\n");
        content.append("资产：").append(alert.getAssetName()).append("(").append(alert.getAssetType()).append(")\n");
        content.append("时间：").append(alert.getStartTime()).append("\n");
        
        if (alert.getMetricName() != null) {
            content.append("指标：").append(alert.getMetricName()).append("\n");
            content.append("当前值：").append(alert.getMetricValue()).append("\n");
            content.append("阈值：").append(alert.getThreshold()).append("\n");
        }
        
        return content.toString();
    }
    
    /**
     * 从DTO生成通知内容
     */
    private String generateContentFromDTO(AlertNotificationDTO dto, NotificationChannel channel) {
        StringBuilder content = new StringBuilder();
        content.append("【告警通知】").append(dto.getSeverity()).append("\n");
        content.append("告警名称：").append(dto.getAlertName()).append("\n");
        content.append("告警内容：").append(dto.getAlertMessage()).append("\n");
        
        if (dto.getAssetName() != null) {
            content.append("资产：").append(dto.getAssetName()).append("(").append(dto.getAssetType()).append(")\n");
        }
        
        content.append("时间：").append(dto.getAlertTime()).append("\n");
        
        if (dto.getMetricName() != null) {
            content.append("指标：").append(dto.getMetricName()).append("\n");
            content.append("当前值：").append(dto.getMetricValue()).append("\n");
            content.append("阈值：").append(dto.getThreshold()).append("\n");
        }
        
        return content.toString();
    }
} 