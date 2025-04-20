package com.skyi.visualization.service;

import com.skyi.visualization.dto.MetricDataDTO;
import com.skyi.visualization.dto.MetricQueryDTO;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 指标查询服务接口
 */
public interface MetricService {

    /**
     * 根据指标名称和查询条件查询时序数据
     *
     * @param queryDTO 查询条件
     * @return 指标数据
     */
    MetricDataDTO queryMetricData(MetricQueryDTO queryDTO);
    
    /**
     * 查询多个指标的数据
     *
     * @param queryDTOList 多个查询条件
     * @return 多个指标数据
     */
    List<MetricDataDTO> queryMultiMetricData(List<MetricQueryDTO> queryDTOList);
    
    /**
     * 查询指标的最新值
     *
     * @param metricName 指标名称
     * @param tags       标签过滤条件
     * @return 最新值
     */
    Double queryLatestValue(String metricName, Map<String, String> tags);
    
    /**
     * 查询指标在时间范围内的统计值
     *
     * @param metricName        指标名称
     * @param tags              标签过滤条件
     * @param startTime         开始时间
     * @param endTime           结束时间
     * @param aggregateFunction 聚合函数
     * @return 统计值
     */
    Double queryAggregateValue(String metricName, Map<String, String> tags, 
                              Instant startTime, Instant endTime, 
                              String aggregateFunction);
    
    /**
     * 查询可用的指标名称列表
     *
     * @return 指标名称列表
     */
    List<String> listMetricNames();
    
    /**
     * 查询指标的标签键列表
     *
     * @param metricName 指标名称
     * @return 标签键列表
     */
    List<String> listTagKeys(String metricName);
    
    /**
     * 查询指标标签键的可能值
     *
     * @param metricName 指标名称
     * @param tagKey     标签键
     * @return 标签值列表
     */
    List<String> listTagValues(String metricName, String tagKey);
} 