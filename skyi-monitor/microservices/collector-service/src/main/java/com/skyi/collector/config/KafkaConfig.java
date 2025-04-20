package com.skyi.collector.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka配置类
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "collector.kafka.enabled", havingValue = "true")
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${collector.kafka.topic.metric-data:metric-data}")
    private String metricDataTopicName;

    @Value("${collector.kafka.topic.collection-status:collection-status}")
    private String collectionStatusTopicName;

    @Value("${collector.kafka.topic.partition-count:3}")
    private int partitionCount;

    @Value("${collector.kafka.topic.replication-factor:1}")
    private short replicationFactor;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * 指标数据主题名称
     */
    @Bean("metricDataTopicName")
    public String metricDataTopicName() {
        return metricDataTopicName;
    }

    /**
     * 采集状态主题名称
     */
    @Bean("collectionStatusTopicName")
    public String collectionStatusTopicName() {
        return collectionStatusTopicName;
    }

    /**
     * 指标数据主题
     */
    @Bean
    public NewTopic metricDataTopic() {
        log.info("创建Kafka主题: {}, 分区数: {}, 副本因子: {}", metricDataTopicName, partitionCount, replicationFactor);
        return TopicBuilder.name(metricDataTopicName)
                .partitions(partitionCount)
                .replicas(replicationFactor)
                .compact()
                .build();
    }

    /**
     * 采集状态主题
     */
    @Bean
    public NewTopic collectionStatusTopic() {
        log.info("创建Kafka主题: {}, 分区数: {}, 副本因子: {}", collectionStatusTopicName, partitionCount, replicationFactor);
        return TopicBuilder.name(collectionStatusTopicName)
                .partitions(partitionCount)
                .replicas(replicationFactor)
                .build();
    }
} 