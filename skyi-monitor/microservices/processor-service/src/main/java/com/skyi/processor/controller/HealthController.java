package com.skyi.processor.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 提供服务健康状态检查的API
 */
@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {
    
    @Value("${spring.application.name}")
    private String serviceName;
    
    @Value("${server.port}")
    private String serverPort;
    
    @Autowired(required = false)
    private com.influxdb.client.InfluxDBClient influxDBClient;
    
    /**
     * 获取服务健康状态
     * 
     * @return 健康状态信息
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        log.debug("接收健康检查请求");
        
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", serviceName);
        status.put("port", serverPort);
        status.put("timestamp", Instant.now().toString());
        status.put("influxdb_connected", isInfluxDbConnected());
        
        // 添加其他服务状态信息
        Map<String, Object> dependencies = new HashMap<>();
        dependencies.put("influxdb", Map.of("status", isInfluxDbConnected() ? "UP" : "DOWN"));
        dependencies.put("kafka", Map.of("status", "UP"));  // 这里可以实际检测Kafka连接状态
        
        status.put("dependencies", dependencies);
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * 详细健康检查
     * 
     * @return 详细的健康状态信息
     */
    @GetMapping("/detail")
    public ResponseEntity<Map<String, Object>> healthDetail() {
        log.debug("接收详细健康检查请求");
        
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", serviceName);
        status.put("port", serverPort);
        status.put("timestamp", Instant.now().toString());
        
        // 添加更详细的服务状态信息
        Map<String, Object> dependencies = new HashMap<>();
        
        // InfluxDB状态
        Map<String, Object> influxStatus = new HashMap<>();
        boolean influxConnected = isInfluxDbConnected();
        influxStatus.put("status", influxConnected ? "UP" : "DOWN");
        influxStatus.put("details", influxConnected ? "连接正常" : "无法连接");
        dependencies.put("influxdb", influxStatus);
        
        // Kafka状态 - 实际应用中可检测Kafka连接
        dependencies.put("kafka", Map.of(
                "status", "UP",
                "details", "连接正常"
        ));
        
        // JVM状态
        Map<String, Object> jvmStatus = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        jvmStatus.put("max_memory", runtime.maxMemory() / (1024 * 1024) + "MB");
        jvmStatus.put("total_memory", runtime.totalMemory() / (1024 * 1024) + "MB");
        jvmStatus.put("free_memory", runtime.freeMemory() / (1024 * 1024) + "MB");
        jvmStatus.put("used_memory", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) + "MB");
        jvmStatus.put("processors", runtime.availableProcessors());
        
        status.put("dependencies", dependencies);
        status.put("jvm", jvmStatus);
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * 检查InfluxDB连接状态
     * 
     * @return 是否连接正常
     */
    private boolean isInfluxDbConnected() {
        if (influxDBClient == null) {
            return false;
        }
        
        try {
            // 执行一个简单查询来验证连接
            influxDBClient.ping();
            return true;
        } catch (Exception e) {
            log.warn("InfluxDB连接检测失败", e);
            return false;
        }
    }
} 