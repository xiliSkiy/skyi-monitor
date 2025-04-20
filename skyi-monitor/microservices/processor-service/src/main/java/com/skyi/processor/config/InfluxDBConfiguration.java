package com.skyi.processor.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * InfluxDB配置类
 */
@Slf4j
@Configuration
@Primary
public class InfluxDBConfiguration {
    
    // 硬编码配置值，仅用于测试
    private static final String URL = "http://localhost:8086";
    private static final String TOKEN = "mytoken";
    private static final String ORG = "skyi";
    private static final String BUCKET = "metrics";
    private static final int BATCH_SIZE = 5000;
    private static final int FLUSH_INTERVAL = 1000;
    
    /**
     * 创建InfluxDB客户端
     */
    @Bean
    @Primary
    public InfluxDBClient influxDBClient() {
        log.info("初始化InfluxDB客户端, URL: {}, Org: {}, Bucket: {}", URL, ORG, BUCKET);
        return InfluxDBClientFactory.create(URL, TOKEN.toCharArray(), ORG, BUCKET);
    }
    
    /**
     * 创建写入API，配置批量写入
     */
    @Bean
    @Primary
    public WriteApi writeApi(InfluxDBClient influxDBClient) {
        WriteOptions writeOptions = WriteOptions.builder()
                .batchSize(BATCH_SIZE)
                .flushInterval(FLUSH_INTERVAL)
                .bufferLimit(BATCH_SIZE * 2)
                .jitterInterval(100)
                .retryInterval(1000)
                .maxRetries(3)
                .maxRetryDelay(5000)
                .maxRetryTime(30000)
                .exponentialBase(2)
                .build();
        
        return influxDBClient.makeWriteApi(writeOptions);
    }
    
    /**
     * 获取组织名称
     */
    @Bean
    public String influxDBOrg() {
        return ORG;
    }
    
    /**
     * 获取存储桶名称
     */
    @Bean
    public String influxDBBucket() {
        return BUCKET;
    }
} 