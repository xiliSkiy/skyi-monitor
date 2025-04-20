package com.skyi.collector.service.collector;

import com.skyi.collector.dto.MetricDataDTO;
import com.skyi.collector.model.MetricDefinition;

import java.util.List;
import java.util.Map;

/**
 * 插件化指标采集器接口
 * 支持基于动态指标定义进行数据采集
 */
public interface MetricCollector {
    
    /**
     * 获取采集器支持的协议
     * 
     * @return 协议名称
     */
    String getProtocol();
    
    /**
     * 获取采集器支持的资产类型列表
     * 
     * @return 资产类型列表
     */
    List<String> getSupportedAssetTypes();
    
    /**
     * 执行采集操作
     * 
     * @param context 采集上下文，包含资产信息、要采集的指标定义等
     * @return 采集结果
     */
    CollectionResult collect(CollectionContext context);
    
    /**
     * 测试连接
     * 
     * @param assetType 资产类型
     * @param connectionParams 连接参数
     * @return 连接测试结果
     */
    ConnectionTestResult testConnection(String assetType, Map<String, Object> connectionParams);
    
    /**
     * 检查采集器是否支持指定的指标定义
     * 
     * @param metricDefinition 指标定义
     * @return 是否支持
     */
    boolean supportsMetric(MetricDefinition metricDefinition);
    
    /**
     * 是否可以批量采集多个指标
     * 
     * @return 是否支持批量采集
     */
    default boolean supportsBatchCollection() {
        return true;
    }
    
    /**
     * 获取采集器优先级，数值越小优先级越高
     * 
     * @return 优先级
     */
    default int getPriority() {
        return 100;
    }
} 