package com.skyi.infrastructure.kafka.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Kafka消费者测试工具类
 * 用于测试从Kafka接收消息
 */
public class KafkaConsumerTester {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String METRIC_DATA_TOPIC = "collector-metric-data";
    private static final String CONSUMER_GROUP = "tester-group";
    private static final AtomicBoolean running = new AtomicBoolean(true);

    public static void main(String[] args) {
        // 创建Kafka消费者配置
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("group.id", CONSUMER_GROUP);
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("auto.offset.reset", "latest");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");

        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("正在关闭消费者...");
            running.set(false);
        }));

        // 创建Kafka消费者
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            // 订阅主题
            consumer.subscribe(Collections.singletonList(METRIC_DATA_TOPIC));
            System.out.println("Kafka消费者已启动，开始监听主题: " + METRIC_DATA_TOPIC);
            System.out.println("按Ctrl+C退出...");

            // 持续轮询消息
            while (running.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    processRecord(record);
                }
            }
        } catch (Exception e) {
            System.err.println("消费消息时发生错误:");
            e.printStackTrace();
        }
    }

    /**
     * 处理接收到的消息记录
     *
     * @param record 消息记录
     */
    private static void processRecord(ConsumerRecord<String, String> record) {
        try {
            System.out.printf("接收到消息: topic=%s, partition=%d, offset=%d, key=%s%n",
                    record.topic(), record.partition(), record.offset(), record.key());
            
            // 解析消息值
            String value = record.value();
            if (value != null && !value.isEmpty()) {
                JSONObject metricData = JSON.parseObject(value);
                System.out.println("指标数据:");
                System.out.println("  - 指标名称: " + metricData.getString("metricName"));
                System.out.println("  - 指标值: " + metricData.getDouble("value"));
                System.out.println("  - 时间戳: " + metricData.getString("timestamp"));
                System.out.println("  - 资产ID: " + metricData.getLong("assetId"));
                System.out.println("  - 任务ID: " + metricData.getLong("taskId"));
                System.out.println("  - 实例ID: " + metricData.getLong("instanceId"));
                
                // 打印标签
                JSONObject tags = metricData.getJSONObject("tags");
                if (tags != null && !tags.isEmpty()) {
                    System.out.println("  - 标签:");
                    for (String key : tags.keySet()) {
                        System.out.printf("    %s: %s%n", key, tags.getString(key));
                    }
                }
                
                // 打印字段
                JSONObject fields = metricData.getJSONObject("fields");
                if (fields != null && !fields.isEmpty()) {
                    System.out.println("  - 字段:");
                    for (String key : fields.keySet()) {
                        System.out.printf("    %s: %s%n", key, fields.get(key));
                    }
                }
            }
            
            System.out.println("-----------------------------------------------------");
        } catch (Exception e) {
            System.err.println("处理消息记录时发生错误: " + e.getMessage());
        }
    }
} 