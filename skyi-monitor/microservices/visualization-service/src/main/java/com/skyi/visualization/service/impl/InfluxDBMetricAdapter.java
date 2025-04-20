package com.skyi.visualization.service.impl;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * InfluxDB度量适配器
 * 适配通用TSDB API和InfluxDB
 */
@Service
@Slf4j
public class InfluxDBMetricAdapter {

    protected final InfluxDBClient influxDBClient;
    protected final QueryApi queryApi;
    
    @Value("${influxdb.bucket:skyi}")
    protected String bucket;
    
    @Value("${influxdb.org:skyi}")
    protected String orgValue;
    
    /**
     * 构造函数
     * @param influxDBClient InfluxDB客户端
     */
    @Autowired
    public InfluxDBMetricAdapter(InfluxDBClient influxDBClient) {
        this.influxDBClient = influxDBClient;
        // 处理null客户端的情况，为子类（如MockAdapter）提供支持
        this.queryApi = influxDBClient != null ? influxDBClient.getQueryApi() : null;
    }
    
    /**
     * 获取可用的指标名称列表
     */
    public List<String> getMetricNames() {
        try {
            if (queryApi == null) {
                log.warn("InfluxDB客户端未配置");
                return Collections.emptyList();
            }
            
            String query = String.format("import \"influxdata/influxdb/schema\"\n" +
                                         "schema.measurements(bucket: \"%s\")", bucket);
            
            log.debug("执行Flux查询: {}", query);
            
            List<FluxTable> tables = queryApi.query(query);
            
            List<String> metrics = new ArrayList<>();
            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    metrics.add(record.getValueByKey("_value").toString());
                }
            }
            
            return metrics;
        } catch (Exception e) {
            log.error("从InfluxDB获取指标名称列表失败", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取标签键列表
     */
    public List<String> getTagKeys(String metricName) {
        try {
            if (queryApi == null) {
                log.warn("InfluxDB客户端未配置");
                return Collections.emptyList();
            }
            
            String query = String.format("import \"influxdata/influxdb/schema\"\n" +
                                         "schema.tagKeys(bucket: \"%s\", measurement: \"%s\")", 
                                         bucket, metricName);
            
            log.debug("执行Flux查询: {}", query);
            
            List<FluxTable> tables = queryApi.query(query);
            
            List<String> tagKeys = new ArrayList<>();
            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    tagKeys.add(record.getValueByKey("_value").toString());
                }
            }
            
            return tagKeys;
        } catch (Exception e) {
            log.error("从InfluxDB获取指标[{}]的标签键列表失败", metricName, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取标签值列表
     */
    public List<String> getTagValues(String metricName, String tagKey) {
        try {
            if (queryApi == null) {
                log.warn("InfluxDB客户端未配置");
                return Collections.emptyList();
            }
            
            String query = String.format("import \"influxdata/influxdb/schema\"\n" +
                                         "schema.tagValues(bucket: \"%s\", measurement: \"%s\", tag: \"%s\")", 
                                         bucket, metricName, tagKey);
            
            log.debug("执行Flux查询: {}", query);
            
            List<FluxTable> tables = queryApi.query(query);
            
            List<String> tagValues = new ArrayList<>();
            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    tagValues.add(record.getValueByKey("_value").toString());
                }
            }
            
            return tagValues;
        } catch (Exception e) {
            log.error("从InfluxDB获取指标[{}]标签[{}]的值列表失败", metricName, tagKey, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 查询时序数据
     */
    public Map<String, Object> queryData(Map<String, Object> queryParams) {
        try {
            if (queryApi == null) {
                log.warn("InfluxDB客户端未配置");
                Map<String, Object> result = new HashMap<>();
                result.put("status", "error");
                result.put("message", "InfluxDB客户端未配置");
                return result;
            }
            
            String metric = (String) queryParams.get("metric");
            Map<String, String> tags = (Map<String, String>) queryParams.getOrDefault("tags", Collections.emptyMap());
            
            // 构建Flux查询
            StringBuilder query = new StringBuilder();
            query.append("from(bucket: \"").append(bucket).append("\")\n");
            query.append("  |> range(start: -1h)\n");  // 默认查询过去1小时的数据
            query.append("  |> filter(fn: (r) => r._measurement == \"").append(metric).append("\")\n");
            
            // 添加标签过滤条件
            for (Map.Entry<String, String> tag : tags.entrySet()) {
                query.append("  |> filter(fn: (r) => r.").append(tag.getKey())
                     .append(" == \"").append(tag.getValue()).append("\")\n");
            }
            
            // 添加其他查询条件（如果有）
            if (queryParams.containsKey("aggregateFunction")) {
                String aggregator = (String) queryParams.get("aggregateFunction");
                String window = (String) queryParams.getOrDefault("interval", "1m");
                
                if ("mean".equals(aggregator)) {
                    query.append("  |> aggregateWindow(every: ").append(window)
                         .append(", fn: mean, createEmpty: false)\n");
                } else if ("max".equals(aggregator)) {
                    query.append("  |> aggregateWindow(every: ").append(window)
                         .append(", fn: max, createEmpty: false)\n");
                } else if ("min".equals(aggregator)) {
                    query.append("  |> aggregateWindow(every: ").append(window)
                         .append(", fn: min, createEmpty: false)\n");
                } else if ("sum".equals(aggregator)) {
                    query.append("  |> aggregateWindow(every: ").append(window)
                         .append(", fn: sum, createEmpty: false)\n");
                }
            }
            
            // 添加限制条件（如果有）
            if (queryParams.containsKey("limit")) {
                Integer limit = (Integer) queryParams.get("limit");
                query.append("  |> limit(n: ").append(limit).append(")\n");
            }
            
            log.debug("执行Flux查询: {}", query.toString());
            
            List<FluxTable> tables = queryApi.query(query.toString());
            
            // 转换查询结果
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            List<List<Object>> datapoints = new ArrayList<>();
            
            double aggregateValue = 0;
            int count = 0;
            
            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    long timestamp = record.getTime().toEpochMilli();
                    double value = ((Number) record.getValue()).doubleValue();
                    
                    datapoints.add(Arrays.asList(timestamp, value));
                    
                    aggregateValue += value;
                    count++;
                }
            }
            
            // 排序数据点（按时间戳）
            datapoints.sort((a, b) -> ((Long) a.get(0)).compareTo((Long) b.get(0)));
            
            data.put("datapoints", datapoints);
            
            // 计算聚合值
            if (count > 0) {
                data.put("aggregate", aggregateValue / count);
            } else {
                data.put("aggregate", 0);
            }
            
            result.put("data", data);
            result.put("status", "success");
            
            return result;
        } catch (Exception e) {
            log.error("从InfluxDB查询时序数据失败", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", e.getMessage());
            
            return result;
        }
    }
} 