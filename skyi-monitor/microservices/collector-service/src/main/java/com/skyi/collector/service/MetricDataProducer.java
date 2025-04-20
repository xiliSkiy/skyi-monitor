package com.skyi.collector.service;

import com.skyi.collector.model.CollectionStatus;
import com.skyi.collector.model.MetricData;

import java.util.List;
import java.util.Map;

/**
 * 指标数据生产者接口
 * 负责将采集到的指标数据和采集状态发送到消息队列
 */
public interface MetricDataProducer {

    /**
     * 发送单条指标数据
     *
     * @param metricData 指标数据
     * @return 发送是否成功
     */
    boolean sendMetricData(MetricData metricData);

    /**
     * 批量发送指标数据
     *
     * @param metricDataList 指标数据列表
     * @return 成功发送的数量
     */
    int sendMetricDataBatch(List<MetricData> metricDataList);

    /**
     * 发送采集状态
     *
     * @param collectionStatus 采集状态
     * @return 发送是否成功
     */
    boolean sendCollectionStatus(CollectionStatus collectionStatus);
    
    /**
     * 获取发送统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getStats();
} 