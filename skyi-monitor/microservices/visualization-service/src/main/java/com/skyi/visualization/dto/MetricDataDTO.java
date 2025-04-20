package com.skyi.visualization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 指标数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricDataDTO {
    /**
     * 指标名称
     */
    private String name;
    
    /**
     * 指标标签
     */
    private Map<String, String> tags;
    
    /**
     * 数据点列表
     */
    private List<DataPoint> dataPoints;
    
    /**
     * 聚合值
     */
    private Double aggregateValue;
    
    /**
     * 数据点（时间-值对）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        /**
         * 时间戳
         */
        private Instant timestamp;
        
        /**
         * 值
         */
        private Double value;
    }
} 