package com.skyi.collector.service.collector;

import com.skyi.collector.model.MetricDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 指标采集器注册表
 * 用于管理所有的指标采集器
 */
@Slf4j
@Component
public class MetricCollectorRegistry {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    /**
     * 协议到采集器的映射，用于快速查找
     */
    private final Map<String, List<MetricCollector>> protocolCollectors = new HashMap<>();
    
    /**
     * 资产类型到采集器的映射，用于快速查找
     */
    private final Map<String, List<MetricCollector>> assetTypeCollectors = new HashMap<>();
    
    /**
     * 所有已注册的采集器
     */
    private final List<MetricCollector> collectors = new ArrayList<>();
    
    /**
     * 初始化，扫描并注册所有实现了MetricCollector接口的Bean
     */
    @PostConstruct
    public void init() {
        Map<String, MetricCollector> collectorBeans = applicationContext.getBeansOfType(MetricCollector.class);
        log.info("发现{}个指标采集器", collectorBeans.size());
        
        for (MetricCollector collector : collectorBeans.values()) {
            registerCollector(collector);
        }
        
        // 按优先级排序采集器
        collectors.sort(Comparator.comparingInt(MetricCollector::getPriority));
        
        // 输出采集器注册情况
        log.info("指标采集器注册完成，共{}个采集器", collectors.size());
        for (String protocol : protocolCollectors.keySet()) {
            log.info("协议[{}]有{}个采集器", protocol, protocolCollectors.get(protocol).size());
        }
        for (String assetType : assetTypeCollectors.keySet()) {
            log.info("资产类型[{}]有{}个采集器", assetType, assetTypeCollectors.get(assetType).size());
        }
    }
    
    /**
     * 注册采集器
     * 
     * @param collector 采集器实例
     */
    public void registerCollector(MetricCollector collector) {
        if (collector == null) {
            return;
        }
        
        // 添加到全局列表
        collectors.add(collector);
        
        // 注册协议
        String protocol = collector.getProtocol();
        protocolCollectors.computeIfAbsent(protocol, k -> new ArrayList<>()).add(collector);
        
        // 注册资产类型
        List<String> assetTypes = collector.getSupportedAssetTypes();
        if (assetTypes != null) {
            for (String assetType : assetTypes) {
                assetTypeCollectors.computeIfAbsent(assetType, k -> new ArrayList<>()).add(collector);
            }
        }
        
        log.info("注册采集器: protocol={}, assetTypes={}, class={}", 
                protocol, assetTypes, collector.getClass().getSimpleName());
    }
    
    /**
     * 获取所有采集器
     * 
     * @return 采集器列表
     */
    public List<MetricCollector> getAllCollectors() {
        return Collections.unmodifiableList(collectors);
    }
    
    /**
     * 根据协议获取采集器
     * 
     * @param protocol 协议名称
     * @return 采集器列表
     */
    public List<MetricCollector> getCollectorsByProtocol(String protocol) {
        return protocolCollectors.getOrDefault(protocol, Collections.emptyList());
    }
    
    /**
     * 根据资产类型获取采集器
     * 
     * @param assetType 资产类型
     * @return 采集器列表
     */
    public List<MetricCollector> getCollectorsByAssetType(String assetType) {
        return assetTypeCollectors.getOrDefault(assetType, Collections.emptyList());
    }
    
    /**
     * 查找支持指定指标定义的采集器
     * 
     * @param metricDefinition 指标定义
     * @return 采集器列表
     */
    public List<MetricCollector> findCollectorsForMetric(MetricDefinition metricDefinition) {
        if (metricDefinition == null) {
            return Collections.emptyList();
        }
        
        // 先获取协议对应的采集器
        List<MetricCollector> protocolMatches = protocolCollectors.getOrDefault(
                metricDefinition.getCollectionMethod(), Collections.emptyList());
        
        // 再过滤出支持该指标的采集器
        return protocolMatches.stream()
                .filter(collector -> collector.supportsMetric(metricDefinition))
                .sorted(Comparator.comparingInt(MetricCollector::getPriority))
                .collect(Collectors.toList());
    }
    
    /**
     * 查找最合适的采集器
     * 
     * @param protocol 协议
     * @param assetType 资产类型
     * @return 最合适的采集器，如果没有找到则返回null
     */
    public MetricCollector findBestCollector(String protocol, String assetType) {
        List<MetricCollector> protocolMatches = protocolCollectors.getOrDefault(protocol, Collections.emptyList());
        
        return protocolMatches.stream()
                .filter(collector -> collector.getSupportedAssetTypes().contains(assetType))
                .min(Comparator.comparingInt(MetricCollector::getPriority))
                .orElse(null);
    }
} 