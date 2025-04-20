package com.skyi.collector.service.collector;

import com.skyi.collector.dto.MetricDataDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 采集结果类
 * 包含采集操作的结果状态和采集到的指标数据
 */
@Data
@Builder
public class CollectionResult {
    /**
     * 采集是否成功
     */
    private boolean success;
    
    /**
     * 采集开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 采集结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 采集的指标数据列表
     */
    @Builder.Default
    private List<MetricDataDTO> metricData = new ArrayList<>();
    
    /**
     * 成功采集的指标数量
     */
    private int successCount;
    
    /**
     * 失败采集的指标数量
     */
    private int failCount;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 指标采集的详细错误信息
     * key: 指标ID, value: 错误信息
     */
    @Builder.Default
    private Map<Long, String> metricErrors = new HashMap<>();
    
    /**
     * 扩展信息，可用于传递额外数据
     */
    @Builder.Default
    private Map<String, Object> extInfo = new HashMap<>();
    
    /**
     * 创建一个成功的采集结果
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param metricData 指标数据
     * @return 采集结果
     */
    public static CollectionResult success(LocalDateTime startTime, LocalDateTime endTime, List<MetricDataDTO> metricData) {
        return CollectionResult.builder()
                .success(true)
                .startTime(startTime)
                .endTime(endTime)
                .metricData(metricData)
                .successCount(metricData.size())
                .failCount(0)
                .build();
    }
    
    /**
     * 创建一个失败的采集结果
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param errorMessage 错误信息
     * @return 采集结果
     */
    public static CollectionResult failure(LocalDateTime startTime, LocalDateTime endTime, String errorMessage) {
        return CollectionResult.builder()
                .success(false)
                .startTime(startTime)
                .endTime(endTime)
                .successCount(0)
                .failCount(0)
                .errorMessage(errorMessage)
                .build();
    }
    
    /**
     * 添加指标数据
     * 
     * @param data 指标数据
     */
    public void addMetricData(MetricDataDTO data) {
        if (metricData == null) {
            metricData = new ArrayList<>();
        }
        metricData.add(data);
        successCount++;
    }
    
    /**
     * 添加指标错误
     * 
     * @param metricId 指标ID
     * @param error 错误信息
     */
    public void addMetricError(Long metricId, String error) {
        if (metricErrors == null) {
            metricErrors = new HashMap<>();
        }
        metricErrors.put(metricId, error);
        failCount++;
    }
} 