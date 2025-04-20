package com.skyi.processor.controller;

import com.skyi.processor.model.MetricData;
import com.skyi.processor.service.MetricProcessorService;
import com.skyi.processor.service.MetricStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * 指标数据控制器
 * 提供指标数据查询的RESTful API
 */
@Slf4j
@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricController {

    private final MetricStorageService metricStorageService;
    private final MetricProcessorService metricProcessorService;

    /**
     * 查询指标数据
     *
     * @param metricName 指标名称
     * @param assetId 资产ID（可选）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param tags 标签（可选，格式：key1=value1,key2=value2）
     * @return 指标数据列表
     */
    @GetMapping
    public ResponseEntity<List<MetricData>> queryMetrics(
            @RequestParam String metricName,
            @RequestParam(required = false) Long assetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String tags) {
        
        // 转换时间
        Instant startInstant = startTime.atZone(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endTime.atZone(ZoneId.systemDefault()).toInstant();
        
        // 解析标签
        Map<String, String> tagMap = parseTags(tags);
        
        // 添加资产ID作为标签（如果提供）
        if (assetId != null) {
            tagMap.put("assetId", assetId.toString());
        }
        
        log.debug("查询指标数据: metricName={}, startTime={}, endTime={}, tags={}", 
                metricName, startInstant, endInstant, tagMap);
        
        // 调用服务查询数据
        List<MetricData> result = metricStorageService.queryMetrics(
                metricName, tagMap, startInstant, endInstant);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 查询聚合指标数据
     *
     * @param metricName 指标名称
     * @param assetId 资产ID（可选）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param aggregationType 聚合类型（avg, max, min, sum）
     * @param interval 聚合时间间隔（秒）
     * @param tags 标签（可选，格式：key1=value1,key2=value2）
     * @return 聚合指标数据列表
     */
    @GetMapping("/aggregated")
    public ResponseEntity<List<MetricData>> queryAggregatedMetrics(
            @RequestParam String metricName,
            @RequestParam(required = false) Long assetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam String aggregationType,
            @RequestParam int interval,
            @RequestParam(required = false) String tags) {
        
        // 转换时间
        Instant startInstant = startTime.atZone(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endTime.atZone(ZoneId.systemDefault()).toInstant();
        
        // 解析标签
        Map<String, String> tagMap = parseTags(tags);
        
        // 添加资产ID作为标签（如果提供）
        if (assetId != null) {
            tagMap.put("assetId", assetId.toString());
        }
        
        log.debug("查询聚合指标数据: metricName={}, startTime={}, endTime={}, aggregationType={}, interval={}s, tags={}", 
                metricName, startInstant, endInstant, aggregationType, interval, tagMap);
        
        // 调用服务查询聚合数据
        List<MetricData> result = metricStorageService.queryAggregatedMetrics(
                metricName, tagMap, startInstant, endInstant, aggregationType, interval);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 手动存储指标数据
     *
     * @param metricData 指标数据
     * @return 处理结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> storeMetric(@RequestBody MetricData metricData) {
        log.debug("接收手动存储指标数据请求: {}", metricData);
        
        // 如果未提供时间戳，使用当前时间
        if (metricData.getTimestamp() == null) {
            metricData.setTimestamp(Instant.now());
        }
        
        // 处理指标数据
        metricProcessorService.processMetric(metricData);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "指标数据已处理");
        response.put("data", metricData);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 手动触发指标聚合
     *
     * @param metricName 指标名称
     * @param interval 聚合时间间隔（分钟）
     * @return 处理结果
     */
    @PostMapping("/aggregate")
    public ResponseEntity<Map<String, Object>> triggerAggregation(
            @RequestParam String metricName,
            @RequestParam(defaultValue = "5") int interval) {
        
        log.info("接收手动触发指标聚合请求: metricName={}, interval={}分钟", metricName, interval);
        
        metricProcessorService.aggregateMetrics(metricName, interval);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", String.format("已触发指标%s的%d分钟聚合操作", metricName, interval));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 解析标签字符串
     *
     * @param tagsStr 标签字符串，格式：key1=value1,key2=value2
     * @return 标签Map
     */
    private Map<String, String> parseTags(String tagsStr) {
        Map<String, String> result = new HashMap<>();
        
        if (tagsStr != null && !tagsStr.isEmpty()) {
            String[] pairs = tagsStr.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    result.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }
        
        return result;
    }
} 