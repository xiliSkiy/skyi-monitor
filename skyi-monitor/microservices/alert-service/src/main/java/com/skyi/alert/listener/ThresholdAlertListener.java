package com.skyi.alert.listener;

import com.skyi.alert.constant.KafkaTopicConstants;
import com.skyi.alert.dto.ThresholdAlertDTO;
import com.skyi.alert.model.Alert;
import com.skyi.alert.service.AlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 阈值告警监听器
 */
@Slf4j
@Component
public class ThresholdAlertListener {

    @Autowired
    private AlertService alertService;

    /**
     * 处理阈值超限告警
     */
    @KafkaListener(topics = KafkaTopicConstants.TOPIC_THRESHOLD_ALERT)
    public void handleThresholdAlert(
            @Payload ThresholdAlertDTO alertDTO,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        
        log.info("接收到阈值告警消息: topic={}, partition={}, offset={}, assetId={}, metricName={}",
                topic, partition, offset, alertDTO.getAssetId(), alertDTO.getMetricName());
        
        try {
            Alert alert = alertService.processThresholdAlert(alertDTO);
            if (alert != null) {
                log.info("阈值告警处理成功: alertId={}, alertUuid={}", alert.getId(), alert.getAlertUuid());
                ack.acknowledge();
            } else {
                log.error("阈值告警处理失败，将重试");
            }
        } catch (Exception e) {
            log.error("处理阈值告警异常", e);
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