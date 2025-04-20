package com.skyi.alert.listener;

import com.skyi.alert.constant.KafkaTopicConstants;
import com.skyi.alert.dto.AlertNotificationDTO;
import com.skyi.alert.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 告警通知监听器
 */
@Slf4j
@Component
public class NotificationListener {

    @Autowired
    private NotificationService notificationService;

    /**
     * 处理告警通知
     */
    @KafkaListener(topics = KafkaTopicConstants.TOPIC_ALERT_NOTIFICATION)
    public void handleAlertNotification(
            @Payload AlertNotificationDTO notificationDTO,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        
        log.info("接收到告警通知消息: topic={}, partition={}, offset={}, alertId={}, alertUuid={}",
                topic, partition, offset, notificationDTO.getAlertId(), notificationDTO.getAlertUuid());
        
        try {
            boolean result = notificationService.processNotification(notificationDTO);
            if (result) {
                log.info("告警通知处理成功: alertId={}, alertUuid={}", 
                        notificationDTO.getAlertId(), notificationDTO.getAlertUuid());
                ack.acknowledge();
            } else {
                log.error("告警通知处理失败，将重试");
            }
        } catch (Exception e) {
            log.error("处理告警通知异常", e);
            if (isRetryableException(e)) {
                log.warn("发生可重试异常，消息将被重新处理");
            } else {
                log.error("发生不可重试异常，消息将被丢弃");
                ack.acknowledge();
            }
        }
    }

    /**
     * 判断异常是否可重试
     */
    private boolean isRetryableException(Exception e) {
        return e instanceof java.net.ConnectException
                || e instanceof java.net.SocketTimeoutException
                || e instanceof java.io.IOException
                || e.getMessage() != null && e.getMessage().contains("timed out");
    }
} 