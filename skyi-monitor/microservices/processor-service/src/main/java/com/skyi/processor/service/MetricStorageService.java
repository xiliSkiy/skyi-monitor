package com.skyi.processor.service;

import com.skyi.processor.model.MetricData;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 指标存储服务接口
 * 定义了与指标数据存储相关的操作
 */
public interface MetricStorageService {

    /**
     * 存储单个指标数据
     * @param metricData 指标数据
     * @return 是否成功存储
     */
    boolean storeMetric(MetricData metricData);

    /**
     * 批量存储指标数据
     * @param metricDataList 指标数据列表
     * @return 成功存储的数据条数
     */
    int storeMetrics(List<MetricData> metricDataList);

    /**
     * 查询指标数据
     * @param metricName 指标名称
     * @param tags 标签条件
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 指标数据列表
     */
    List<MetricData> queryMetrics(String metricName, Map<String, String> tags, 
                                 Instant startTime, Instant endTime);
    
    /**
     * 查询指标数据，支持结果数量限制
     * @param metricName 指标名称
     * @param tags 标签条件
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 最大结果数量，0表示不限制
     * @return 指标数据列表
     */
    List<MetricData> queryMetrics(String metricName, Map<String, String> tags, 
                                 Instant startTime, Instant endTime, int limit);

    /**
     * 查询聚合指标数据
     * @param metricName 指标名称
     * @param tags 标签条件
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param aggregationType 聚合类型，如mean、max、min、sum等
     * @param timeInterval 时间间隔，单位为秒
     * @return 聚合后的指标数据列表
     */
    List<MetricData> queryAggregatedMetrics(String metricName, Map<String, String> tags, 
                                           Instant startTime, Instant endTime,
                                           String aggregationType, int timeInterval);

    /**
     * 对指标数据列表执行聚合计算
     * @param metricDataList 指标数据列表
     * @param aggregationType 聚合类型，如mean、max、min、sum等
     * @return 聚合结果值
     */
    Double aggregateMetrics(List<MetricData> metricDataList, String aggregationType);
    
    /**
     * 删除指标数据
     * @param metricName 指标名称
     * @param tags 标签条件
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 删除操作影响的数据条数，InfluxDB可能返回1表示操作成功
     */
    int deleteMetrics(String metricName, Map<String, String> tags, 
                     Instant startTime, Instant endTime);
} 