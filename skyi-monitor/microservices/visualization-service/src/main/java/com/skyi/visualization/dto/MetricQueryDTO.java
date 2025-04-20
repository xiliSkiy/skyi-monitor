package com.skyi.visualization.dto;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * 指标查询参数DTO
 */
@Data
public class MetricQueryDTO {
    /**
     * 指标名称
     */
    private String name;
    
    /**
     * 查询标签（用于过滤）
     */
    private Map<String, String> tags;
    
    /**
     * 查询开始时间
     */
    private Instant startTime;
    
    /**
     * 查询结束时间
     */
    private Instant endTime;
    
    /**
     * 聚合函数（如：avg, sum, min, max, count）
     */
    private String aggregateFunction;
    
    /**
     * 时间间隔（如：1m, 5m, 1h）
     */
    private String interval;
    
    /**
     * 结果限制条数
     */
    private Integer limit;
    
    /**
     * 排序方式（asc, desc）
     */
    private String order;
    
    /**
     * 数据填充方式
     */
    private String fill;
} 