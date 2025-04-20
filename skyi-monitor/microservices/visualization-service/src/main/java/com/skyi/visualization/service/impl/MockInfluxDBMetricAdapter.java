package com.skyi.visualization.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 模拟的InfluxDB度量适配器
 * 用于在没有真实InfluxDB的环境中进行测试
 */
@Service
@Slf4j
@Profile("dev-mock")
@Primary
@ConditionalOnProperty(name = "influxdb.enabled", havingValue = "false")
public class MockInfluxDBMetricAdapter extends InfluxDBMetricAdapter {

    /**
     * 默认构造函数
     */
    public MockInfluxDBMetricAdapter() {
        super(null); // 不需要实际的InfluxDBClient
    }
    
    /**
     * 获取可用的指标名称列表
     */
    @Override
    public List<String> getMetricNames() {
        log.info("模拟获取指标名称列表");
        
        return Arrays.asList(
            "system.cpu.usage",
            "system.memory.usage",
            "system.disk.io",
            "system.network.traffic",
            "http.server.requests",
            "jvm.memory.used",
            "jvm.gc.pause",
            "logback.events"
        );
    }
    
    /**
     * 获取标签键列表
     */
    @Override
    public List<String> getTagKeys(String metricName) {
        log.info("模拟获取指标[{}]的标签键列表", metricName);
        
        return Arrays.asList(
            "instance",
            "application",
            "host",
            "region",
            "service"
        );
    }
    
    /**
     * 获取标签值列表
     */
    @Override
    public List<String> getTagValues(String metricName, String tagKey) {
        log.info("模拟获取指标[{}]标签[{}]的值列表", metricName, tagKey);
        
        List<String> tagValues = new ArrayList<>();
        
        if ("instance".equals(tagKey)) {
            tagValues.addAll(Arrays.asList("instance1", "instance2", "instance3"));
        } else if ("application".equals(tagKey)) {
            tagValues.addAll(Arrays.asList("visualization-service", "storage-service", "collector-service"));
        } else if ("host".equals(tagKey)) {
            tagValues.addAll(Arrays.asList("host1", "host2", "host3"));
        } else if ("region".equals(tagKey)) {
            tagValues.addAll(Arrays.asList("beijing", "shanghai", "guangzhou"));
        } else if ("service".equals(tagKey)) {
            tagValues.addAll(Arrays.asList("api", "web", "gateway"));
        }
        
        return tagValues;
    }
    
    /**
     * 查询时序数据
     */
    @Override
    public Map<String, Object> queryData(Map<String, Object> queryParams) {
        log.info("模拟查询时序数据: {}", queryParams);
        
        String metric = (String) queryParams.get("metric");
        
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        
        // 生成随机数据点
        List<List<Object>> datapoints = new ArrayList<>();
        long now = System.currentTimeMillis();
        Random random = new Random();
        
        for (int i = 0; i < 10; i++) {
            long timestamp = now - (9 - i) * 60000; // 每分钟一个点
            double value = random.nextDouble() * 100;
            datapoints.add(Arrays.asList(timestamp, value));
        }
        
        data.put("datapoints", datapoints);
        data.put("aggregate", random.nextDouble() * 100);
        result.put("data", data);
        result.put("status", "success");
        
        return result;
    }
} 