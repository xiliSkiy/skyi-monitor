package com.skyi.processor.service.consumer;

import com.alibaba.fastjson.JSON;
import com.skyi.processor.model.MetricData;
import com.skyi.processor.service.MetricProcessorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 指标数据消费者
 * 负责从Kafka中消费指标数据并进行处理
 */
@Slf4j
@Service
public class MetricDataConsumer {

    @Autowired
    private MetricProcessorService metricProcessorService;

    @Value("${processor.kafka.batch-size:50}")
    private int batchSize;

    /**
     * 消费单条指标数据
     *
     * @param record 消费记录
     * @param ack 确认对象
     */
    @KafkaListener(topics = "${processor.kafka.topics.metric-data}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMetricData(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            String value = record.value();
            if (!StringUtils.hasText(value)) {
                log.warn("接收到空消息，忽略处理");
                ack.acknowledge();
                return;
            }

            log.debug("接收到指标数据消息: {}", value);
            MetricData metricData = JSON.parseObject(value, MetricData.class);
            
            if (metricData != null) {
                metricProcessorService.processMetric(metricData);
                log.debug("成功处理指标数据: {}", metricData.getMetricName());
            }
            
            // 确认消息已处理
            ack.acknowledge();
        } catch (Exception e) {
            log.error("处理指标数据消息失败", e);
            // 出现异常也确认消息，避免同一消息反复失败
            // 实际应用中可以考虑重试机制或死信队列
            ack.acknowledge();
        }
    }

    /**
     * 批量消费指标数据
     *
     * @param records 消费记录列表
     * @param ack 确认对象
     */
    @KafkaListener(topics = "${processor.kafka.topics.metric-data}", 
                   groupId = "${spring.kafka.consumer.group-id}", 
                   id = "batch-consumer")
    public void consumeMetricDataBatch(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        if (records == null || records.isEmpty()) {
            ack.acknowledge();
            return;
        }
        
        log.debug("批量接收到{}条指标数据消息", records.size());
        List<MetricData> metricDataList = new ArrayList<>(records.size());
        
        try {
            // 解析消息
            for (ConsumerRecord<String, String> record : records) {
                String value = record.value();
                if (StringUtils.hasText(value)) {
                    try {
                        MetricData metricData = JSON.parseObject(value, MetricData.class);
                        if (metricData != null) {
                            metricDataList.add(metricData);
                        }
                    } catch (Exception e) {
                        log.warn("解析指标数据消息失败: {}", value, e);
                    }
                }
            }
            
            // 批量处理
            if (!metricDataList.isEmpty()) {
                metricProcessorService.processMetrics(metricDataList);
                log.debug("成功批量处理{}条指标数据", metricDataList.size());
            }
            
            // 确认消息已处理
            ack.acknowledge();
        } catch (Exception e) {
            log.error("批量处理指标数据消息失败，共{}条", records.size(), e);
            // 出现异常也确认消息
            ack.acknowledge();
        }
    }
} 