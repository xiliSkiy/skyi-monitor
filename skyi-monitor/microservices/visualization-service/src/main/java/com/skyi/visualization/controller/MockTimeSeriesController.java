package com.skyi.visualization.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 模拟时序数据库API的控制器
 * 用于在没有真实时序数据库服务的情况下进行测试
 * 只在dev-mock配置下启用
 */
@RestController
@RequestMapping("/api/v1")
@Slf4j
@Profile("dev-mock")
public class MockTimeSeriesController {

    /**
     * 获取可用的指标名称列表
     */
    @GetMapping("/metadata/metrics")
    public ResponseEntity<List<String>> getMetricNames() {
        log.info("获取指标名称列表");
        
        List<String> metrics = Arrays.asList(
            "system.cpu.usage",
            "system.memory.usage",
            "system.disk.io",
            "system.network.traffic",
            "http.server.requests",
            "jvm.memory.used",
            "jvm.gc.pause",
            "logback.events"
        );
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * 获取标签键列表
     */
    @GetMapping("/metadata/tagk")
    public ResponseEntity<List<String>> getTagKeys(@RequestParam("metric") String metricName) {
        log.info("获取指标[{}]的标签键列表", metricName);
        
        List<String> tagKeys = Arrays.asList(
            "instance",
            "application",
            "host",
            "region",
            "service"
        );
        
        return ResponseEntity.ok(tagKeys);
    }
    
    /**
     * 获取标签值列表
     */
    @GetMapping("/metadata/tagv")
    public ResponseEntity<List<String>> getTagValues(
            @RequestParam("metric") String metricName,
            @RequestParam("tagk") String tagKey) {
        log.info("获取指标[{}]标签[{}]的值列表", metricName, tagKey);
        
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
        
        return ResponseEntity.ok(tagValues);
    }
    
    /**
     * 查询时序数据
     */
    @PostMapping("/timeseries/query")
    public ResponseEntity<Map<String, Object>> queryData(@RequestBody Map<String, Object> queryParams) {
        log.info("查询时序数据: {}", queryParams);
        
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
        
        return ResponseEntity.ok(result);
    }
} 