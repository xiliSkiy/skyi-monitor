package com.skyi.infrastructure.kafka.utils;

import com.alibaba.fastjson.JSON;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Kafka生产者测试工具类
 * 用于测试向Kafka发送消息
 */
public class KafkaProducerTester {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String METRIC_DATA_TOPIC = "collector-metric-data";

    public static void main(String[] args) {
        // 创建Kafka生产者配置
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        props.put("acks", "1");
        props.put("retries", 3);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);

        // 创建Kafka生产者
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            System.out.println("Kafka生产者已创建，准备发送测试消息...");

            // 发送10条测试消息
            for (int i = 0; i < 10; i++) {
                // 创建测试指标数据
                Map<String, Object> metricData = createTestMetricData(i);
                
                // 将数据转换为JSON字符串
                String key = UUID.randomUUID().toString();
                String value = JSON.toJSONString(metricData);
                
                // 发送消息
                ProducerRecord<String, String> record = new ProducerRecord<>(METRIC_DATA_TOPIC, key, value);
                Future<RecordMetadata> future = producer.send(record);
                
                // 获取发送结果
                RecordMetadata metadata = future.get();
                System.out.printf("消息已发送: topic=%s, partition=%d, offset=%d, key=%s%n",
                        metadata.topic(), metadata.partition(), metadata.offset(), key);
                
                // 暂停一秒
                Thread.sleep(1000);
            }
            
            System.out.println("测试消息发送完成。");
        } catch (Exception e) {
            System.err.println("发送消息时发生错误:");
            e.printStackTrace();
        }
    }

    /**
     * 创建测试指标数据
     * 
     * @param index 消息索引
     * @return 指标数据Map
     */
    private static Map<String, Object> createTestMetricData(int index) {
        Map<String, Object> metricData = new HashMap<>();
        metricData.put("metricName", "cpu.usage");
        metricData.put("value", 50.0 + index * 2.5);
        metricData.put("timestamp", Instant.now().toString());
        metricData.put("assetId", 1001L);
        metricData.put("taskId", 2001L);
        metricData.put("instanceId", 3001L);
        
        // 添加标签
        Map<String, String> tags = new HashMap<>();
        tags.put("host", "server-" + (index % 3 + 1));
        tags.put("core", "core-" + (index % 4));
        tags.put("env", "test");
        metricData.put("tags", tags);
        
        // 添加额外字段
        Map<String, Object> fields = new HashMap<>();
        fields.put("processCount", 120 + index);
        fields.put("threadCount", 1200 + index * 10);
        fields.put("memoryUsage", 35.5 + index * 1.5);
        metricData.put("fields", fields);
        
        return metricData;
    }
} 