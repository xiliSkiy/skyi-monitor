package com.skyi.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Map;

/**
 * 时间序列查询DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesQueryDTO {

    /**
     * 开始时间
     */
    private Instant startTime;
    
    /**
     * 结束时间
     */
    private Instant endTime;
    
    /**
     * 资产ID
     */
    private String assetId;
    
    /**
     * 指标名称
     */
    private String metricName;
    
    /**
     * 聚合函数 (如 mean, max, min, sum)
     */
    private String aggregateFunction;
    
    /**
     * 时间窗口 (如 1m, 5m, 1h)
     */
    private String window;
    
    /**
     * 查询标签
     */
    private Map<String, String> tags;
    
    /**
     * 分页大小
     */
    @Builder.Default
    private int limit = 1000;
    
    /**
     * 排序字段
     */
    @Builder.Default
    private String orderBy = "time";
    
    /**
     * 排序方向
     */
    @Builder.Default
    private String orderDirection = "DESC";
} 