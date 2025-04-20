package com.skyi.processor.service.impl;

import com.skyi.processor.model.MetricData;
import com.skyi.processor.service.MetricProcessorService;
import com.skyi.processor.service.MetricStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * 指标处理服务实现类
 */
@Slf4j
@Service
public class MetricProcessorServiceImpl implements MetricProcessorService {

    /**
     * 指标存储服务
     */
    private final MetricStorageService metricStorageService;
    
    /**
     * 指标阈值配置，用于异常检测
     * key: 指标名称, value: Map<String, Double>，内部map为阈值配置
     * 内部map的键为"min"和"max"，值为最小值和最大值
     */
    private final Map<String, Map<String, Double>> metricThresholds = new ConcurrentHashMap<>();
    
    private final Executor processorThreadPool;
    
    /**
     * 构造函数，使用@Qualifier指定线程池
     */
    @Autowired
    public MetricProcessorServiceImpl(MetricStorageService metricStorageService, 
                                    @Qualifier("processorTaskExecutor") Executor processorThreadPool) {
        this.metricStorageService = metricStorageService;
        this.processorThreadPool = processorThreadPool;
    }
    
    /**
     * 处理单个指标数据
     */
    @Override
    public void processMetric(MetricData metricData) {
        try {
            log.debug("处理指标数据: {}", metricData);
            
            // 执行数据处理逻辑
            MetricData processedData = processMetricData(metricData);
            
            // 异步存储处理后的数据
            CompletableFuture.runAsync(() -> {
                try {
                    metricStorageService.storeMetric(processedData);
                } catch (Exception e) {
                    log.error("存储指标数据失败: {}", processedData.getMetricName(), e);
                }
            }, processorThreadPool);
        } catch (Exception e) {
            log.error("处理指标数据失败: {}", metricData.getMetricName(), e);
        }
    }
    
    /**
     * 批量处理指标数据
     * 实现接口要求的方法
     */
    @Override
    @Async("processorTaskExecutor")
    public void processMetrics(List<MetricData> metricDataList) {
        // 直接调用批量处理方法
        processBatchMetrics(metricDataList);
    }
    
    /**
     * 批量处理指标数据
     */
    @Override
    @Async("processorTaskExecutor")
    public void processBatchMetrics(List<MetricData> metricDataList) {
        log.debug("批量处理指标数据, 数量: {}", metricDataList.size());
        
        // 处理每条指标数据
        List<MetricData> processedDataList = metricDataList.stream()
                .map(this::processMetricData)
                .collect(Collectors.toList());
        
        // 批量存储处理后的数据
        CompletableFuture.runAsync(() -> {
            try {
                metricStorageService.storeMetrics(processedDataList);
            } catch (Exception e) {
                log.error("批量存储指标数据失败, 数量: {}", processedDataList.size(), e);
            }
        }, processorThreadPool);
    }

    /**
     * 数据校验
     */
    private void validateMetricData(MetricData metricData) {
        if (metricData == null) {
            throw new IllegalArgumentException("指标数据不能为空");
        }
        
        if (StringUtils.isBlank(metricData.getMetricName())) {
            throw new IllegalArgumentException("指标名称不能为空");
        }
        
        if (metricData.getValue() == null) {
            throw new IllegalArgumentException("指标值不能为空");
        }
        
        if (metricData.getTimestamp() == null) {
            log.warn("指标时间戳为空，使用当前时间: {}", metricData.getMetricName());
            metricData.setTimestamp(Instant.now());
        }
    }
    
    /**
     * 检查指标数据是否有效
     */
    private boolean isValidMetricData(MetricData metricData) {
        return metricData != null && 
               StringUtils.isNotBlank(metricData.getMetricName()) && 
               metricData.getValue() != null;
    }
    
    @Override
    public MetricData applyTransformations(MetricData metricData) {
        if (metricData == null) {
            return null;
        }
        
        // 处理特定类型的指标
        switch (metricData.getMetricName()) {
            case "cpu_usage":
                return processCpuMetric(metricData);
                
            case "memory_usage":
                return processMemoryMetric(metricData);
                
            case "disk_usage":
                return processDiskMetric(metricData);
                
            case "network_traffic":
                return processNetworkMetric(metricData);
                
            default:
                // 默认不做特殊处理
                return metricData;
        }
    }
    
    /**
     * 处理CPU指标
     */
    private MetricData processCpuMetric(MetricData metricData) {
        // 确保单位为百分比
        metricData.addField("unit", "%");
        
        // 处理特殊情况，某些采集可能返回小于0或大于100的值
        double value = metricData.getValue();
        if (value < 0) {
            metricData.setValue(0.0);
        } else if (value > 100) {
            metricData.setValue(100.0);
        }
        
        // 添加额外标签
        if (metricData.getTags().containsKey("core")) {
            metricData.addTag("metric_type", "cpu_core");
        } else {
            metricData.addTag("metric_type", "cpu_total");
        }
        
        return metricData;
    }
    
    /**
     * 处理内存指标
     */
    private MetricData processMemoryMetric(MetricData metricData) {
        // 确保单位为百分比
        metricData.addField("unit", "%");
        
        // 处理特殊情况，某些采集可能返回小于0或大于100的值
        double value = metricData.getValue();
        if (value < 0) {
            metricData.setValue(0.0);
        } else if (value > 100) {
            metricData.setValue(100.0);
        }
        
        // 添加额外标签
        metricData.addTag("metric_type", "memory");
        
        return metricData;
    }
    
    /**
     * 处理磁盘指标
     */
    private MetricData processDiskMetric(MetricData metricData) {
        // 确保单位为百分比
        metricData.addField("unit", "%");
        
        // 处理特殊情况，某些采集可能返回小于0或大于100的值
        double value = metricData.getValue();
        if (value < 0) {
            metricData.setValue(0.0);
        } else if (value > 100) {
            metricData.setValue(100.0);
        }
        
        // 添加额外标签
        metricData.addTag("metric_type", "disk");
        
        // 确保磁盘路径信息
        if (!metricData.getTags().containsKey("mount_point") && 
                metricData.getFields().containsKey("mountPoint")) {
            Object mountPoint = metricData.getFields().get("mountPoint");
            if (mountPoint != null) {
                metricData.addTag("mount_point", mountPoint.toString());
            }
        }
        
        return metricData;
    }
    
    /**
     * 处理网络指标
     */
    private MetricData processNetworkMetric(MetricData metricData) {
        // 根据字段确定正确的单位
        if (metricData.getFields().containsKey("unit")) {
            metricData.addField("unit", metricData.getFields().get("unit").toString());
        } else {
            metricData.addField("unit", "KB/s");
        }
        
        // 添加额外标签
        metricData.addTag("metric_type", "network");
        
        // 确保网络接口信息
        if (!metricData.getTags().containsKey("interface") && 
                metricData.getFields().containsKey("interface")) {
            Object iface = metricData.getFields().get("interface");
            if (iface != null) {
                metricData.addTag("interface", iface.toString());
            }
        }
        
        // 确保方向信息（上传/下载）
        if (!metricData.getTags().containsKey("direction") && 
                metricData.getFields().containsKey("direction")) {
            Object direction = metricData.getFields().get("direction");
            if (direction != null) {
                metricData.addTag("direction", direction.toString());
            }
        }
        
        return metricData;
    }
    
    /**
     * 检测异常值
     */
    @Override
    public boolean detectAnomaly(MetricData metricData) {
        if (metricData == null || metricData.getValue() == null) {
            return false;
        }
        
        String metricName = metricData.getMetricName();
        double value = metricData.getValue();
        
        // 获取指标的阈值配置
        Map<String, Double> thresholds = metricThresholds.get(metricName);
        if (thresholds == null) {
            // 使用默认阈值
            thresholds = getDefaultThresholds(metricName);
        }
        
        // 检查是否超出阈值
        Double minThreshold = thresholds.get("min");
        Double maxThreshold = thresholds.get("max");
        
        boolean isAnomaly = false;
        if (minThreshold != null && value < minThreshold) {
            log.debug("指标{}值{}低于最小阈值{}", metricName, value, minThreshold);
            isAnomaly = true;
        } else if (maxThreshold != null && value > maxThreshold) {
            log.debug("指标{}值{}超过最大阈值{}", metricName, value, maxThreshold);
            isAnomaly = true;
        }
        
        return isAnomaly;
    }
    
    /**
     * 获取指标的默认阈值
     */
    private Map<String, Double> getDefaultThresholds(String metricName) {
        Map<String, Double> thresholds = new HashMap<>();
        
        switch (metricName) {
            case "cpu_usage":
                thresholds.put("min", 0.0);
                thresholds.put("max", 90.0);
                break;
                
            case "memory_usage":
                thresholds.put("min", 0.0);
                thresholds.put("max", 90.0);
                break;
                
            case "disk_usage":
                thresholds.put("min", 0.0);
                thresholds.put("max", 85.0);
                break;
                
            default:
                // 默认阈值
                thresholds.put("min", null);  // 不设置最小阈值
                thresholds.put("max", null);  // 不设置最大阈值
        }
        
        return thresholds;
    }
    
    /**
     * 聚合指标数据
     */
    @Override
    public void aggregateMetrics(String metricName, int interval) {
        if (StringUtils.isBlank(metricName) || interval <= 0) {
            log.warn("聚合指标参数无效: metricName={}, interval={}", metricName, interval);
            return;
        }
        
        try {
            log.info("开始聚合指标数据: metricName={}, interval={}分钟", metricName, interval);
            
            // 计算时间范围
            Instant endTime = Instant.now();
            Instant startTime = endTime.minus(interval * 2, ChronoUnit.MINUTES);
            
            // 查询原始数据
            List<MetricData> rawData = metricStorageService.queryMetrics(
                    metricName, null, startTime, endTime);
            
            if (rawData.isEmpty()) {
                log.info("指标{}没有可聚合的数据", metricName);
                return;
            }
            
            // 按资产ID和标签分组
            Map<String, List<MetricData>> groupedData = rawData.stream()
                    .collect(Collectors.groupingBy(this::getMetricGroupKey));
            
            // 对每个分组进行聚合计算
            List<MetricData> aggregatedDataList = new ArrayList<>();
            
            for (Map.Entry<String, List<MetricData>> entry : groupedData.entrySet()) {
                List<MetricData> groupData = entry.getValue();
                
                // 计算聚合值（平均值、最大值、最小值）
                double avgValue = groupData.stream()
                        .mapToDouble(MetricData::getValue)
                        .average()
                        .orElse(0.0);
                
                double maxValue = groupData.stream()
                        .mapToDouble(MetricData::getValue)
                        .max()
                        .orElse(0.0);
                
                double minValue = groupData.stream()
                        .mapToDouble(MetricData::getValue)
                        .min()
                        .orElse(0.0);
                
                // 创建聚合数据点
                if (!groupData.isEmpty()) {
                    MetricData template = groupData.get(0);
                    
                    // 平均值
                    MetricData avgMetric = new MetricData()
                            .setMetricName(metricName + "_" + interval + "m_avg")
                            .setValue(avgValue)
                            .setTimestamp(endTime)
                            .setAssetId(template.getAssetId())
                            .setTaskId(template.getTaskId());
                    
                    // 复制标签
                    template.getTags().forEach(avgMetric::addTag);
                    avgMetric.addTag("aggregation", "avg");
                    avgMetric.addTag("interval", interval + "m");
                    
                    // 添加到聚合列表
                    aggregatedDataList.add(avgMetric);
                    
                    // 最大值
                    MetricData maxMetric = new MetricData()
                            .setMetricName(metricName + "_" + interval + "m_max")
                            .setValue(maxValue)
                            .setTimestamp(endTime)
                            .setAssetId(template.getAssetId())
                            .setTaskId(template.getTaskId());
                    
                    template.getTags().forEach(maxMetric::addTag);
                    maxMetric.addTag("aggregation", "max");
                    maxMetric.addTag("interval", interval + "m");
                    
                    aggregatedDataList.add(maxMetric);
                    
                    // 最小值
                    MetricData minMetric = new MetricData()
                            .setMetricName(metricName + "_" + interval + "m_min")
                            .setValue(minValue)
                            .setTimestamp(endTime)
                            .setAssetId(template.getAssetId())
                            .setTaskId(template.getTaskId());
                    
                    template.getTags().forEach(minMetric::addTag);
                    minMetric.addTag("aggregation", "min");
                    minMetric.addTag("interval", interval + "m");
                    
                    aggregatedDataList.add(minMetric);
                }
            }
            
            // 存储聚合数据
            if (!aggregatedDataList.isEmpty()) {
                metricStorageService.storeMetrics(aggregatedDataList);
                log.info("成功聚合并存储指标数据: metricName={}, interval={}分钟, 共{}个聚合数据点",
                        metricName, interval, aggregatedDataList.size());
            }
            
        } catch (Exception e) {
            log.error("聚合指标数据失败: metricName={}, interval={}分钟", metricName, interval, e);
        }
    }
    
    /**
     * 获取指标分组键
     * 按资产ID和重要标签进行分组
     */
    private String getMetricGroupKey(MetricData metricData) {
        StringBuilder key = new StringBuilder();
        
        // 添加资产ID
        key.append("asset_").append(metricData.getAssetId()).append("_");
        
        // 添加重要标签
        Map<String, String> tags = metricData.getTags();
        if (tags != null) {
            if (tags.containsKey("host")) {
                key.append("host_").append(tags.get("host")).append("_");
            }
            
            if (tags.containsKey("core")) {
                key.append("core_").append(tags.get("core")).append("_");
            }
            
            if (tags.containsKey("mount_point")) {
                key.append("mount_").append(tags.get("mount_point")).append("_");
            }
            
            if (tags.containsKey("interface")) {
                key.append("if_").append(tags.get("interface")).append("_");
            }
            
            if (tags.containsKey("direction")) {
                key.append("dir_").append(tags.get("direction"));
            }
        }
        
        return key.toString();
    }

    /**
     * 处理单条指标数据
     * 执行数据转换、计算、聚合等操作
     *
     * @param metricData 原始指标数据
     * @return 处理后的指标数据
     */
    private MetricData processMetricData(MetricData metricData) {
        // 根据业务需求进行数据处理
        // 例如：单位转换、阈值检查、数据清洗等
        
        // 添加处理时间标签
        metricData.addTag("processed_time", System.currentTimeMillis() + "");
        
        // 示例：对某些特定指标进行特殊处理
        if ("cpu_usage".equals(metricData.getMetricName())) {
            // CPU使用率需要确保在0-100范围内
            Double value = metricData.getValue();
            if (value != null) {
                if (value > 100) {
                    metricData.setValue(100.0);
                } else if (value < 0) {
                    metricData.setValue(0.0);
                }
            }
        }
        
        return metricData;
    }
} 