package com.skyi.alert.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * InfluxDB配置类
 */
@Configuration
@ConditionalOnProperty(name = "influxdb.enabled", havingValue = "true", matchIfMissing = true)
public class InfluxDBConfig {

    @Value("${influxdb.url}")
    private String url;

    @Value("${influxdb.token}")
    private String token;

    @Value("${influxdb.org}")
    private String org;

    @Value("${influxdb.bucket}")
    private String bucket;

    /**
     * 创建InfluxDB客户端
     */
    @Bean
    public InfluxDBClient influxDBClient() {
        return InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
    }

    /**
     * 创建InfluxDB写入API
     */
    @Bean
    public WriteApi writeApi(InfluxDBClient influxDBClient) {
        return influxDBClient.getWriteApi();
    }

    /**
     * 创建InfluxDB查询API
     */
    @Bean
    public QueryApi queryApi(InfluxDBClient influxDBClient) {
        return influxDBClient.getQueryApi();
    }
} 