package com.skyi.processor.service;

import com.skyi.processor.model.MetricData;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 指标处理服务接口
 * 定义与指标数据处理相关的操作
 */
public interface MetricProcessorService {

    /**
     * 处理单个指标数据
     * 
     * @param metricData 指标数据
     */
    void processMetric(MetricData metricData);
    
    /**
     * 批量处理多个指标数据
     * 
     * @param metricDataList 指标数据列表
     */
    void processMetrics(List<MetricData> metricDataList);
    
    /**
     * 批量处理指标数据
     * 兼容旧版本API，内部调用processMetrics方法
     * 
     * @param metricDataList 指标数据列表
     */
    void processBatchMetrics(List<MetricData> metricDataList);
    
    /**
     * 应用数据转换
     * 根据不同指标类型应用不同的转换规则
     * 
     * @param metricData 原始指标数据
     * @return 转换后的指标数据
     */
    MetricData applyTransformations(MetricData metricData);
    
    /**
     * 检测异常数据
     * 基于指标阈值判断是否为异常值
     * 
     * @param metricData 指标数据
     * @return 是否异常
     */
    boolean detectAnomaly(MetricData metricData);
    
    /**
     * 聚合指标数据
     * 按指定的时间间隔聚合数据
     * 
     * @param metricName 指标名称
     * @param interval 聚合时间间隔(分钟)
     */
    void aggregateMetrics(String metricName, int interval);
} 