package com.skyi.processor.service.impl;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.DeletePredicateRequest;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.skyi.processor.model.MetricData;
import com.skyi.processor.service.MetricStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * InfluxDB指标存储服务实现类
 * 使用InfluxDB时序数据库实现指标数据的存储和查询
 */
@Slf4j
@Service
public class InfluxDBMetricStorageServiceImpl implements MetricStorageService {

    @Autowired
    private InfluxDBClient influxDBClient;
    
    @Autowired
    @Qualifier("influxDBBucket")
    private String influxDBBucket;
    
    @Autowired
    @Qualifier("influxDBOrg")
    private String influxDBOrg;
    
    // 硬编码默认值，用于测试
    private static final int BATCH_SIZE = 1000;
    private static final String RAW_RETENTION = "7d";
    
    @Autowired
    @Qualifier("storageTaskExecutor")
    private org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor storageTaskExecutor;

    @Override
    public boolean storeMetric(MetricData metricData) {
        if (metricData == null || StringUtils.isEmpty(metricData.getMetricName())) {
            log.warn("无效的指标数据，跳过存储");
            return false;
        }
        
        try {
            Point point = Point.measurement(metricData.getMetricName())
                    .time(metricData.getTimestamp().toEpochMilli(), WritePrecision.MS)
                    .addField("value", metricData.getValue());
            
            // 添加所有标签
            if (metricData.getTags() != null) {
                metricData.getTags().forEach((key, value) -> {
                    if (key != null && value != null) {
                        point.addTag(key, value);
                    }
                });
            }
            
            // 添加所有额外字段
            if (metricData.getFields() != null) {
                metricData.getFields().forEach((key, value) -> {
                    if (key != null && value != null) {
                        if (value instanceof Number) {
                            point.addField(key, (Number) value);
                        } else if (value instanceof Boolean) {
                            point.addField(key, (Boolean) value);
                        } else {
                            point.addField(key, value.toString());
                        }
                    }
                });
            }
            
            try (WriteApi writeApi = influxDBClient.getWriteApi()) {
                writeApi.writePoint(influxDBBucket, influxDBOrg, point);
            }
            log.debug("成功存储指标: {}", metricData.getMetricName());
            return true;
        } catch (Exception e) {
            log.error("存储指标失败: {}, 原因: {}", metricData.getMetricName(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Async("storageTaskExecutor")
    public int storeMetrics(List<MetricData> metricDataList) {
        if (CollectionUtils.isEmpty(metricDataList)) {
            return 0;
        }

        int successCount = 0;
        try (WriteApi writeApi = influxDBClient.getWriteApi()) {
            // 过滤掉无效的数据
            List<MetricData> validDataList = metricDataList.stream()
                    .filter(this::isValidMetricData)
                    .collect(Collectors.toList());
            
            if (validDataList.isEmpty()) {
                return 0;
            }
            
            // 按批次处理数据，避免一次性处理太多数据
            int totalSize = validDataList.size();
            for (int i = 0; i < totalSize; i += BATCH_SIZE) {
                int endIndex = Math.min(i + BATCH_SIZE, totalSize);
                List<MetricData> batch = validDataList.subList(i, endIndex);
                
                // 转换为Point对象并写入
                List<Point> points = batch.stream()
                        .map(this::convertToPoint)
                        .collect(Collectors.toList());
                
                writeApi.writePoints(influxDBBucket, influxDBOrg, points);
                successCount += batch.size();
                
                log.debug("成功存储批次指标数据: {}/{}", successCount, totalSize);
            }
            
            log.info("成功批量存储指标数据: 总计 {}/{} 条", successCount, metricDataList.size());
            return successCount;
        } catch (Exception e) {
            log.error("批量存储指标数据失败: {}/{} 条, 错误: {}", 
                    successCount, metricDataList.size(), e.getMessage(), e);
            return successCount;
        }
    }

    @Override
    public List<MetricData> queryMetrics(String metricName, Map<String, String> tags, 
                                        Instant startTime, Instant endTime) {
        return queryMetrics(metricName, tags, startTime, endTime, 0);
    }
    
    @Override
    public List<MetricData> queryMetrics(String metricName, Map<String, String> tags, 
                                        Instant startTime, Instant endTime, int limit) {
        if (StringUtils.isEmpty(metricName)) {
            log.warn("指标名称不能为空");
            return Collections.emptyList();
        }
        
        try {
            String fluxQuery = buildFluxQuery(metricName, tags, startTime, endTime, limit);
            List<FluxTable> tables = influxDBClient.getQueryApi().query(fluxQuery, influxDBOrg);
            return convertFluxTablesToMetricData(tables);
        } catch (Exception e) {
            log.error("查询指标数据失败: {}, 原因: {}", metricName, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 构建Flux查询语句
     */
    private String buildFluxQuery(String metricName, Map<String, String> tags, 
                                 Instant startTime, Instant endTime, int limit) {
        StringBuilder queryBuilder = new StringBuilder();
        
        // 基本查询语句
        queryBuilder.append("from(bucket: \"").append(influxDBBucket).append("\") ");
        
        // 添加时间范围
        if (startTime != null && endTime != null) {
            queryBuilder.append("|> range(start: ").append(startTime.toString())
                       .append(", stop: ").append(endTime.toString()).append(") ");
        } else if (startTime != null) {
            queryBuilder.append("|> range(start: ").append(startTime.toString()).append(") ");
        } else {
            // 默认查询最近1小时数据
            queryBuilder.append("|> range(start: -1h) ");
        }
        
        // 添加指标名称过滤
        queryBuilder.append("|> filter(fn: (r) => r._measurement == \"")
                  .append(metricName).append("\") ");
        
        // 添加标签过滤
        if (tags != null && !tags.isEmpty()) {
            tags.forEach((key, value) -> {
                if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(value)) {
                    queryBuilder.append("|> filter(fn: (r) => r.").append(key)
                              .append(" == \"").append(value.replace("\"", "\\\"")).append("\") ");
                }
            });
        }
        
        // 添加结果限制
        if (limit > 0) {
            queryBuilder.append("|> limit(n: ").append(limit).append(") ");
        }
        
        return queryBuilder.toString();
    }

    @Override
    public List<MetricData> queryAggregatedMetrics(String metricName, Map<String, String> tags, 
                                                 Instant startTime, Instant endTime, 
                                                 String aggregationType, int timeInterval) {
        if (StringUtils.isEmpty(metricName) || StringUtils.isEmpty(aggregationType)) {
            log.warn("查询聚合指标数据参数无效: 指标名称={}, 聚合类型={}", metricName, aggregationType);
            return Collections.emptyList();
        }
        
        if (startTime == null || endTime == null) {
            // 默认查询最近1天的数据
            endTime = Instant.now();
            startTime = endTime.minus(1, ChronoUnit.DAYS);
            log.debug("聚合查询时间范围未指定，使用默认范围: {} - {}", startTime, endTime);
        }
        
        if (timeInterval <= 0) {
            // 默认按1小时聚合
            timeInterval = 3600;
            log.debug("聚合间隔未指定，使用默认间隔: {}秒", timeInterval);
        }

        try {
            QueryApi queryApi = influxDBClient.getQueryApi();
            
            // 构建Flux查询语句
            StringBuilder fluxQueryBuilder = new StringBuilder();
            fluxQueryBuilder.append(String.format(
                    "from(bucket: \"%s\") " +
                    "|> range(start: %s, stop: %s) " +
                    "|> filter(fn: (r) => r._measurement == \"%s\") ",
                    influxDBBucket, 
                    startTime.toString(), 
                    endTime.toString(), 
                    metricName
            ));
            
            // 添加标签过滤条件
            if (tags != null && !tags.isEmpty()) {
                tags.forEach((key, value) -> {
                    if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(value)) {
                        fluxQueryBuilder.append(String.format("|> filter(fn: (r) => r.%s == \"%s\") ", 
                                key, value.replace("\"", "\\\"")));
                    }
                });
            }
            
            // 添加聚合函数
            String windowDuration = timeInterval + "s";
            fluxQueryBuilder.append(String.format(
                    "|> aggregateWindow(every: %s, fn: %s, createEmpty: false) " +
                    "|> yield(name: \"%s_%s\")",
                    windowDuration, aggregationType, metricName, aggregationType
            ));
            
            String fluxQuery = fluxQueryBuilder.toString();
            log.debug("执行聚合Flux查询: {}", fluxQuery);
            
            List<FluxTable> tables = queryApi.query(fluxQuery, influxDBOrg);
            List<MetricData> result = convertFluxTablesToMetricData(tables);
            
            log.debug("查询聚合指标数据完成: 指标名称={}, 聚合类型={}, 结果数量={}", 
                    metricName, aggregationType, result.size());
            return result;
        } catch (Exception e) {
            log.error("查询聚合指标数据失败: 指标名称={}, 聚合类型={}, 错误: {}", 
                    metricName, aggregationType, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public Double aggregateMetrics(List<MetricData> metricDataList, String aggregationType) {
        if (CollectionUtils.isEmpty(metricDataList)) {
            return null;
        }
        
        // 提取值列表
        List<Double> values = metricDataList.stream()
                .map(MetricData::getValue)
                .filter(v -> v != null)
                .collect(Collectors.toList());
        
        if (values.isEmpty()) {
            return null;
        }
        
        // 根据聚合类型执行不同的计算
        switch (aggregationType.toLowerCase()) {
            case "mean":
            case "avg":
                return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                
            case "max":
                return values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                
            case "min":
                return values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                
            case "sum":
                return values.stream().mapToDouble(Double::doubleValue).sum();
                
            case "count":
                return (double) values.size();
                
            default:
                log.warn("不支持的聚合类型: {}, 使用平均值代替", aggregationType);
                return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }
    }

    @Override
    public int deleteMetrics(String metricName, Map<String, String> tags, 
                            Instant startTime, Instant endTime) {
        if (StringUtils.isEmpty(metricName)) {
            log.warn("删除指标数据参数无效: 指标名称为空");
            return 0;
        }
        
        if (startTime == null || endTime == null) {
            log.warn("删除指标数据参数无效: 时间范围未指定");
            return 0;
        }

        try {
            // 将Instant转换为OffsetDateTime
            OffsetDateTime startOdt = startTime.atOffset(ZoneOffset.UTC);
            OffsetDateTime endOdt = endTime.atOffset(ZoneOffset.UTC);
            
            // 构建删除谓词
            DeletePredicateRequest request = new DeletePredicateRequest()
                    .predicate(String.format("_measurement=\"%s\"", metricName))
                    .start(startOdt)
                    .stop(endOdt);
            
            // 添加标签谓词
            if (tags != null && !tags.isEmpty()) {
                StringBuilder tagPredicate = new StringBuilder();
                tags.forEach((key, value) -> {
                    if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(value)) {
                        if (tagPredicate.length() > 0) {
                            tagPredicate.append(" AND ");
                        }
                        tagPredicate.append(String.format("%s=\"%s\"", 
                                key, value.replace("\"", "\\\"")));
                    }
                });
                
                if (tagPredicate.length() > 0) {
                    request.predicate(request.getPredicate() + " AND " + tagPredicate.toString());
                }
            }
            
            // 执行删除操作
            influxDBClient.getDeleteApi().delete(request, influxDBBucket, influxDBOrg);
            log.info("成功删除指标数据: 指标名称={}, 时间范围: {} - {}", metricName, startTime, endTime);
            
            // InfluxDB API不返回删除的记录数，这里返回1表示操作成功
            return 1;
        } catch (Exception e) {
            log.error("删除指标数据失败: 指标名称={}, 错误: {}", metricName, e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * 检查指标数据是否有效
     */
    private boolean isValidMetricData(MetricData metricData) {
        return metricData != null && 
               !StringUtils.isEmpty(metricData.getMetricName()) && 
               metricData.getValue() != null && 
               !Double.isNaN(metricData.getValue()) && 
               !Double.isInfinite(metricData.getValue());
    }
    
    /**
     * 将MetricData转换为InfluxDB的Point对象
     */
    private Point convertToPoint(MetricData metricData) {
        Point point = Point.measurement(metricData.getMetricName())
                .time(metricData.getTimestamp().toEpochMilli(), WritePrecision.MS)
                .addField("value", metricData.getValue());

        // 添加所有字段
        Map<String, Object> fields = metricData.getFields();
        if (fields != null && !fields.isEmpty()) {
            fields.forEach((key, value) -> {
                if (value != null) {
                    if (value instanceof Number) {
                        point.addField(key, (Number) value);
                    } else if (value instanceof Boolean) {
                        point.addField(key, (Boolean) value);
                    } else {
                        point.addField(key, value.toString());
                    }
                }
            });
        }

        // 添加标签
        if (metricData.getAssetId() != null) {
            point.addTag("assetId", metricData.getAssetId().toString());
        }
        
        if (metricData.getTaskId() != null) {
            point.addTag("taskId", metricData.getTaskId().toString());
        }
        
        if (metricData.getInstanceId() != null) {
            point.addTag("instanceId", metricData.getInstanceId().toString());
        }

        // 添加其他标签
        Map<String, String> tags = metricData.getTags();
        if (tags != null && !tags.isEmpty()) {
            tags.forEach((key, value) -> {
                if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(value)) {
                    point.addTag(key, value);
                }
            });
        }

        return point;
    }
    
    /**
     * 将InfluxDB的查询结果转换为MetricData列表
     */
    private List<MetricData> convertFluxTablesToMetricData(List<FluxTable> tables) {
        List<MetricData> result = new ArrayList<>();
        
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                MetricData metricData = new MetricData();
                
                // 设置指标名称
                metricData.setMetricName(record.getMeasurement());
                
                // 设置时间戳
                metricData.setTimestamp(record.getTime());
                
                // 设置值
                Object value = record.getValueByKey("_value");
                if (value instanceof Number) {
                    metricData.setValue(((Number) value).doubleValue());
                } else if (value != null) {
                    try {
                        metricData.setValue(Double.parseDouble(value.toString()));
                    } catch (NumberFormatException e) {
                        log.warn("无法解析值为Double: {}", value);
                        metricData.setValue(0.0);
                    }
                } else {
                    metricData.setValue(0.0);
                }
                
                // 设置标签和字段
                Map<String, String> tags = new HashMap<>();
                Map<String, Object> fields = new HashMap<>();
                
                record.getValues().forEach((key, val) -> {
                    if (val != null) {
                        // 跳过InfluxDB内部字段
                        if (key.startsWith("_") && !key.equals("_value")) {
                            return;
                        }
                        
                        // 区分标签和字段
                        if (val instanceof String) {
                            tags.put(key, (String) val);
                        } else {
                            fields.put(key, val);
                        }
                    }
                });
                
                // 解析assetId、taskId和instanceId
                if (tags.containsKey("assetId")) {
                    try {
                        metricData.setAssetId(Long.parseLong(tags.get("assetId")));
                    } catch (NumberFormatException e) {
                        log.warn("无法解析assetId: {}", tags.get("assetId"));
                    }
                }
                
                if (tags.containsKey("taskId")) {
                    try {
                        metricData.setTaskId(Long.parseLong(tags.get("taskId")));
                    } catch (NumberFormatException e) {
                        log.warn("无法解析taskId: {}", tags.get("taskId"));
                    }
                }
                
                if (tags.containsKey("instanceId")) {
                    try {
                        metricData.setInstanceId(Long.parseLong(tags.get("instanceId")));
                    } catch (NumberFormatException e) {
                        log.warn("无法解析instanceId: {}", tags.get("instanceId"));
                    }
                }
                
                metricData.setTags(tags);
                metricData.setFields(fields);
                
                result.add(metricData);
            }
        }
        
        return result;
    }
} 