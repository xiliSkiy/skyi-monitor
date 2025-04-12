package com.skyi.collector.service.collector;

import com.skyi.collector.dto.MetricDataDTO;
import com.skyi.collector.model.CollectorTask;

import java.util.List;
import java.util.Map;

/**
 * 采集器接口
 */
public interface Collector {
    
    /**
     * 获取采集器类型
     * 
     * @return 采集器类型
     */
    String getType();
    
    /**
     * 获取支持的协议
     * 
     * @return 支持的协议列表
     */
    List<String> getSupportedProtocols();
    
    /**
     * 测试连接
     * 
     * @param connectionParams 连接参数
     * @return 是否连接成功
     */
    boolean testConnection(Map<String, String> connectionParams);
    
    /**
     * 执行采集任务
     * 
     * @param task 采集任务
     * @param instanceId 任务实例ID
     * @return 采集数据结果
     */
    List<MetricDataDTO> collect(CollectorTask task, Long instanceId);
    
    /**
     * 校验采集指标配置
     * 
     * @param metrics 采集指标配置
     * @return 校验结果，null表示校验通过，否则返回错误信息
     */
    String validateMetrics(String metrics);
    
    /**
     * 检查采集器是否支持指定的任务
     * 
     * @param task 采集任务
     * @return 是否支持
     */
    boolean supports(CollectorTask task);
} 