package com.skyi.collector.service.collector;

import com.skyi.collector.model.CollectorTask;
import com.skyi.collector.model.MetricDefinition;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 采集上下文对象
 * 包含资产信息、要采集的指标定义以及采集参数等
 */
@Data
@Builder
public class CollectionContext {
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 任务实例ID
     */
    private Long instanceId;
    
    /**
     * 资产ID
     */
    private Long assetId;
    
    /**
     * 资产类型
     */
    private String assetType;
    
    /**
     * 资产名称
     */
    private String assetName;
    
    /**
     * 资产IP
     */
    private String assetIp;
    
    /**
     * 要采集的指标定义列表
     */
    private List<MetricDefinition> metrics;
    
    /**
     * 指标定义与协议映射关系
     * key: 指标定义ID, value: 协议映射信息
     */
    private Map<Long, MetricProtocolMappingInfo> protocolMappings;
    
    /**
     * 连接参数
     */
    private Map<String, Object> connectionParams;
    
    /**
     * 上下文变量，可在采集过程中传递临时数据
     */
    private Map<String, Object> variables;
    
    /**
     * 原始采集任务对象（用于兼容旧的采集器）
     */
    private CollectorTask originalTask;

    /**
     * 指标协议映射信息内部类
     */
    @Data
    @Builder
    public static class MetricProtocolMappingInfo {
        /**
         * 映射ID
         */
        private Long id;
        
        /**
         * 指标定义ID
         */
        private Long metricId;
        
        /**
         * 协议类型
         */
        private String protocol;
        
        /**
         * 指标路径(如SNMP的OID)
         */
        private String path;
        
        /**
         * 解析表达式(支持脚本或公式)
         */
        private String expression;
        
        /**
         * 额外参数(JSON格式)
         */
        private Map<String, Object> parameters;
    }
} 