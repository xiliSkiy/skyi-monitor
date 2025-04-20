package com.skyi.visualization.controller;

import com.skyi.visualization.dto.MetricDataDTO;
import com.skyi.visualization.dto.MetricQueryDTO;
import com.skyi.visualization.service.MetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 可视化指标控制器
 * 提供指标数据查询的REST API
 */
@RestController
@RequestMapping("/api/visualization/metrics")
public class VisualizationMetricsController {

    private final MetricService metricService;

    @Autowired
    public VisualizationMetricsController(MetricService metricService) {
        this.metricService = metricService;
    }
    
    /**
     * 查询指标数据
     * @param queryDTO 查询参数
     * @return 指标数据
     */
    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> queryMetricData(@RequestBody MetricQueryDTO queryDTO) {
        try {
            MetricDataDTO data = metricService.queryMetricData(queryDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "成功");
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 查询多个指标数据
     * @param queryDTOList 查询参数列表
     * @return 多个指标数据
     */
    @PostMapping("/multi-query")
    public ResponseEntity<Map<String, Object>> queryMultiMetricData(@RequestBody List<MetricQueryDTO> queryDTOList) {
        try {
            List<MetricDataDTO> dataList = metricService.queryMultiMetricData(queryDTOList);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "成功");
            response.put("data", dataList);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取指标最新值
     * @param name 指标名称
     * @param tags 指标标签（格式：key1=value1,key2=value2）
     * @return 最新值
     */
    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatestMetricValue(
            @RequestParam String name,
            @RequestParam(required = false) Map<String, String> tags) {
        try {
            Double value = metricService.queryLatestValue(name, tags);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "成功");
            response.put("data", value);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取指标名称列表
     * @return 指标名称列表
     */
    @GetMapping("/names")
    public ResponseEntity<Map<String, Object>> getMetricNames() {
        try {
            List<String> names = metricService.listMetricNames();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "成功");
            response.put("data", names);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取指标标签键列表
     * @param metricName 指标名称
     * @return 标签键列表
     */
    @GetMapping("/tag-keys")
    public ResponseEntity<Map<String, Object>> getMetricTagKeys(@RequestParam String metricName) {
        try {
            List<String> tagKeys = metricService.listTagKeys(metricName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "成功");
            response.put("data", tagKeys);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取指标标签值列表
     * @param metricName 指标名称
     * @param tagKey 标签键
     * @return 标签值列表
     */
    @GetMapping("/tag-values")
    public ResponseEntity<Map<String, Object>> getMetricTagValues(
            @RequestParam String metricName, @RequestParam String tagKey) {
        try {
            List<String> tagValues = metricService.listTagValues(metricName, tagKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "成功");
            response.put("data", tagValues);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败: " + e.getMessage());
            
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
        response.put("service", "visualization-metrics-service");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
} 