package com.skyi.storage.listener;

import com.skyi.storage.constant.KafkaTopicConstants;
import com.skyi.storage.dto.TimeSeriesDataDTO;
import com.skyi.storage.service.TimeSeriesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 时间序列数据消息监听器
 */
@Slf4j
@Component
public class TimeSeriesDataListener {

    @Autowired
    private TimeSeriesService timeSeriesService;

    /**
     * 处理单条时间序列数据
     *
     * @param data     数据
     * @param topic    主题
     * @param partition 分区
     * @param offset   偏移量
     * @param ack      确认对象
     */
    @KafkaListener(topics = KafkaTopicConstants.TOPIC_TIMESERIES_DATA)
    public void handleTimeSeriesData(
            @Payload TimeSeriesDataDTO data,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        
        log.info("接收到时间序列数据: topic={}, partition={}, offset={}, data={}", 
                topic, partition, offset, data);
        
        try {
            boolean result = timeSeriesService.saveData(data);
            if (result) {
                log.info("时间序列数据保存成功");
                ack.acknowledge();
            } else {
                log.error("时间序列数据保存失败，将重试");
                // 消息未确认，Kafka会在配置的重试时间后重新发送
            }
        } catch (Exception e) {
            log.error("处理时间序列数据异常", e);
            // 根据异常类型决定是否重试或丢弃
            if (isRetryableException(e)) {
                log.warn("发生可重试异常，消息将被重新处理");
            } else {
                log.error("发生不可重试异常，消息将被丢弃");
                ack.acknowledge();
            }
        }
    }

    /**
     * 处理批量时间序列数据
     *
     * @param dataList  数据列表
     * @param topic     主题
     * @param partition 分区
     * @param offset    偏移量
     * @param ack       确认对象
     */
    @KafkaListener(topics = KafkaTopicConstants.TOPIC_TIMESERIES_BATCH)
    public void handleTimeSeriesBatch(
            @Payload List<TimeSeriesDataDTO> dataList,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        
        log.info("接收到批量时间序列数据: topic={}, partition={}, offset={}, size={}", 
                topic, partition, offset, dataList.size());
        
        try {
            boolean result = timeSeriesService.saveBatchData(dataList);
            if (result) {
                log.info("批量时间序列数据保存成功");
                ack.acknowledge();
            } else {
                log.error("批量时间序列数据保存失败，将重试");
                // 消息未确认，Kafka会在配置的重试时间后重新发送
            }
        } catch (Exception e) {
            log.error("处理批量时间序列数据异常", e);
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
     *
     * @param e 异常
     * @return 是否可重试
     */
    private boolean isRetryableException(Exception e) {
        // 网络相关异常、服务暂时不可用等通常是可以重试的
        return e instanceof java.net.ConnectException
                || e instanceof java.net.SocketTimeoutException
                || e instanceof java.io.IOException
                || e.getMessage() != null && e.getMessage().contains("timed out");
    }
} 