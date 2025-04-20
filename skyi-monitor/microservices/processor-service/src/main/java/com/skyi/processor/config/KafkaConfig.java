package com.skyi.processor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Kafka配置类
 */
@Slf4j
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${processor.kafka.topics.metric-data}")
    private String metricDataTopic;
    
    @Value("${processor.kafka.topics.collection-status}")
    private String collectionStatusTopic;
    
    /**
     * 配置Kafka监听器工厂
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory);
        
        // 配置手动确认模式
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // 配置错误处理器，出错后等待1秒，最多重试3次
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, exception) -> {
                    log.error("处理消息失败: {}, 异常: {}", record, exception.getMessage(), exception);
                },
                new FixedBackOff(1000L, 3L)
        );
        
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
    
    /**
     * 获取指标数据主题名称
     */
    @Bean("metricDataTopic")
    public String metricDataTopic() {
        return metricDataTopic;
    }
    
    /**
     * 获取采集状态主题名称
     */
    @Bean("collectionStatusTopic")
    public String collectionStatusTopic() {
        return collectionStatusTopic;
    }
} 