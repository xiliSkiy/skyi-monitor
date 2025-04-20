package com.skyi.visualization.controller;

import com.skyi.visualization.service.MetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 度量指标控制器
 */
@RestController
@RequestMapping("/api/metrics")
public class MetricController {

    private final MetricService metricService;

    @Autowired
    public MetricController(MetricService metricService) {
        this.metricService = metricService;
    }

    /**
     * 获取可用的度量指标列表
     * @return 度量指标列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAvailableMetrics() {
        try {
            List<String> metrics = metricService.listMetricNames();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", metrics);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 健康检查接口
     * @return 服务状态信息
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "metric-service");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
} 