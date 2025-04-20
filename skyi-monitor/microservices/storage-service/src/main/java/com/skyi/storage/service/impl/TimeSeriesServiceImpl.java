package com.skyi.storage.service.impl;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.skyi.storage.dto.TimeSeriesDataDTO;
import com.skyi.storage.dto.TimeSeriesQueryDTO;
import com.skyi.storage.model.TimeSeriesData;
import com.skyi.storage.service.TimeSeriesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 时间序列服务实现
 */
@Slf4j
@Service
public class TimeSeriesServiceImpl implements TimeSeriesService {

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String organization;

    @Autowired
    private InfluxDBClient influxDBClient;

    @Autowired
    private WriteApi writeApi;

    @Override
    public boolean saveData(TimeSeriesDataDTO dataDTO) {
        try {
            TimeSeriesData data = convertToEntity(dataDTO);
            writeApi.writeMeasurement(WritePrecision.NS, data);
            return true;
        } catch (Exception e) {
            log.error("保存时间序列数据失败", e);
            return false;
        }
    }

    @Override
    public boolean saveBatchData(List<TimeSeriesDataDTO> dataDTOList) {
        try {
            List<TimeSeriesData> dataList = dataDTOList.stream()
                    .map(this::convertToEntity)
                    .collect(Collectors.toList());
            writeApi.writeMeasurements(WritePrecision.NS, dataList);
            return true;
        } catch (Exception e) {
            log.error("批量保存时间序列数据失败", e);
            return false;
        }
    }

    @Override
    public List<TimeSeriesData> queryData(TimeSeriesQueryDTO queryDTO) {
        try {
            StringBuilder fluxQuery = new StringBuilder();
            fluxQuery.append("from(bucket: \"").append(bucket).append("\") ");
            
            // 添加时间范围过滤
            if (queryDTO.getStartTime() != null && queryDTO.getEndTime() != null) {
                fluxQuery.append("|> range(start: ").append(queryDTO.getStartTime().toString()).append(", ");
                fluxQuery.append("stop: ").append(queryDTO.getEndTime().toString()).append(") ");
            } else if (queryDTO.getStartTime() != null) {
                fluxQuery.append("|> range(start: ").append(queryDTO.getStartTime().toString()).append(") ");
            } else {
                fluxQuery.append("|> range(start: -1h) "); // 默认查询最近1小时的数据
            }
            
            // 添加measurement过滤
            fluxQuery.append("|> filter(fn: (r) => r[\"_measurement\"] == \"metric_data\") ");
            
            // 添加资产ID过滤
            if (StringUtils.hasText(queryDTO.getAssetId())) {
                fluxQuery.append("|> filter(fn: (r) => r[\"assetId\"] == \"").append(queryDTO.getAssetId()).append("\") ");
            }
            
            // 添加指标名称过滤
            if (StringUtils.hasText(queryDTO.getMetricName())) {
                fluxQuery.append("|> filter(fn: (r) => r[\"metricName\"] == \"").append(queryDTO.getMetricName()).append("\") ");
            }
            
            // 添加标签过滤
            if (queryDTO.getTags() != null && !queryDTO.getTags().isEmpty()) {
                for (Map.Entry<String, String> entry : queryDTO.getTags().entrySet()) {
                    fluxQuery.append("|> filter(fn: (r) => r[\"").append(entry.getKey()).append("\"] == \"").append(entry.getValue()).append("\") ");
                }
            }
            
            // 添加聚合函数
            if (StringUtils.hasText(queryDTO.getAggregateFunction()) && StringUtils.hasText(queryDTO.getWindow())) {
                fluxQuery.append("|> aggregateWindow(every: ").append(queryDTO.getWindow()).append(", ");
                fluxQuery.append("fn: ").append(queryDTO.getAggregateFunction()).append(", createEmpty: false) ");
            }
            
            // 添加排序
            if (StringUtils.hasText(queryDTO.getOrderBy())) {
                fluxQuery.append("|> sort(columns: [\"").append(queryDTO.getOrderBy()).append("\"], ");
                fluxQuery.append("desc: ").append("DESC".equalsIgnoreCase(queryDTO.getOrderDirection())).append(") ");
            }
            
            // 添加分页
            if (queryDTO.getLimit() > 0) {
                fluxQuery.append("|> limit(n: ").append(queryDTO.getLimit()).append(")");
            }
            
            log.debug("Generated Flux query: {}", fluxQuery);
            
            QueryApi queryApi = influxDBClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(fluxQuery.toString(), organization);
            
            List<TimeSeriesData> resultList = new ArrayList<>();
            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    TimeSeriesData data = new TimeSeriesData();
                    data.setTime(record.getTime());
                    data.setAssetId(record.getValueByKey("assetId").toString());
                    data.setMetricName(record.getValueByKey("metricName").toString());
                    data.setValue(Double.parseDouble(record.getValue().toString()));
                    if (record.getValueByKey("unit") != null) {
                        data.setUnit(record.getValueByKey("unit").toString());
                    }
                    
                    // 提取其他标签
                    Map<String, String> tags = new HashMap<>();
                    record.getValues().forEach((key, value) -> {
                        if (!key.equals("_time") && !key.equals("_value") && !key.equals("_measurement") &&
                                !key.equals("assetId") && !key.equals("metricName") && !key.equals("unit")) {
                            tags.put(key, value.toString());
                        }
                    });
                    data.setTags(tags);
                    
                    resultList.add(data);
                }
            }
            
            return resultList;
        } catch (Exception e) {
            log.error("查询时间序列数据失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean deleteData(String assetId, String metricName, String startTime, String endTime) {
        try {
            StringBuilder fluxQuery = new StringBuilder();
            fluxQuery.append("from(bucket: \"").append(bucket).append("\") ");
            fluxQuery.append("|> range(start: ").append(startTime).append(", ");
            fluxQuery.append("stop: ").append(endTime).append(") ");
            fluxQuery.append("|> filter(fn: (r) => r[\"_measurement\"] == \"metric_data\") ");
            
            if (StringUtils.hasText(assetId)) {
                fluxQuery.append("|> filter(fn: (r) => r[\"assetId\"] == \"").append(assetId).append("\") ");
            }
            
            if (StringUtils.hasText(metricName)) {
                fluxQuery.append("|> filter(fn: (r) => r[\"metricName\"] == \"").append(metricName).append("\") ");
            }
            
            fluxQuery.append("|> to(bucket: \"_monitoring_trash\", org: \"").append(organization).append("\") ");
            
            QueryApi queryApi = influxDBClient.getQueryApi();
            queryApi.query(fluxQuery.toString(), organization);
            
            return true;
        } catch (Exception e) {
            log.error("删除时间序列数据失败", e);
            return false;
        }
    }

    @Override
    public TimeSeriesData queryLatestData(String assetId, String metricName) {
        try {
            StringBuilder fluxQuery = new StringBuilder();
            fluxQuery.append("from(bucket: \"").append(bucket).append("\") ");
            fluxQuery.append("|> range(start: -1h) "); // 查询最近1小时的数据
            fluxQuery.append("|> filter(fn: (r) => r[\"_measurement\"] == \"metric_data\") ");
            
            if (StringUtils.hasText(assetId)) {
                fluxQuery.append("|> filter(fn: (r) => r[\"assetId\"] == \"").append(assetId).append("\") ");
            }
            
            if (StringUtils.hasText(metricName)) {
                fluxQuery.append("|> filter(fn: (r) => r[\"metricName\"] == \"").append(metricName).append("\") ");
            }
            
            fluxQuery.append("|> last()");
            
            QueryApi queryApi = influxDBClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(fluxQuery.toString(), organization);
            
            if (tables.isEmpty() || tables.get(0).getRecords().isEmpty()) {
                return null;
            }
            
            FluxRecord record = tables.get(0).getRecords().get(0);
            TimeSeriesData data = new TimeSeriesData();
            data.setTime(record.getTime());
            data.setAssetId(record.getValueByKey("assetId").toString());
            data.setMetricName(record.getValueByKey("metricName").toString());
            data.setValue(Double.parseDouble(record.getValue().toString()));
            if (record.getValueByKey("unit") != null) {
                data.setUnit(record.getValueByKey("unit").toString());
            }
            
            // 提取其他标签
            Map<String, String> tags = new HashMap<>();
            record.getValues().forEach((key, value) -> {
                if (!key.equals("_time") && !key.equals("_value") && !key.equals("_measurement") &&
                        !key.equals("assetId") && !key.equals("metricName") && !key.equals("unit")) {
                    tags.put(key, value.toString());
                }
            });
            data.setTags(tags);
            
            return data;
        } catch (Exception e) {
            log.error("查询最新时间序列数据失败", e);
            return null;
        }
    }

    @Override
    public List<Object> groupByMetric(TimeSeriesQueryDTO queryDTO) {
        try {
            StringBuilder fluxQuery = new StringBuilder();
            fluxQuery.append("from(bucket: \"").append(bucket).append("\") ");
            
            // 添加时间范围过滤
            if (queryDTO.getStartTime() != null && queryDTO.getEndTime() != null) {
                fluxQuery.append("|> range(start: ").append(queryDTO.getStartTime().toString()).append(", ");
                fluxQuery.append("stop: ").append(queryDTO.getEndTime().toString()).append(") ");
            } else if (queryDTO.getStartTime() != null) {
                fluxQuery.append("|> range(start: ").append(queryDTO.getStartTime().toString()).append(") ");
            } else {
                fluxQuery.append("|> range(start: -1h) "); // 默认查询最近1小时的数据
            }
            
            // 添加measurement过滤
            fluxQuery.append("|> filter(fn: (r) => r[\"_measurement\"] == \"metric_data\") ");
            
            // 添加资产ID过滤
            if (StringUtils.hasText(queryDTO.getAssetId())) {
                fluxQuery.append("|> filter(fn: (r) => r[\"assetId\"] == \"").append(queryDTO.getAssetId()).append("\") ");
            }
            
            // 添加指标名称过滤
            if (StringUtils.hasText(queryDTO.getMetricName())) {
                fluxQuery.append("|> filter(fn: (r) => r[\"metricName\"] == \"").append(queryDTO.getMetricName()).append("\") ");
            }
            
            // 添加标签过滤
            if (queryDTO.getTags() != null && !queryDTO.getTags().isEmpty()) {
                for (Map.Entry<String, String> entry : queryDTO.getTags().entrySet()) {
                    fluxQuery.append("|> filter(fn: (r) => r[\"").append(entry.getKey()).append("\"] == \"").append(entry.getValue()).append("\") ");
                }
            }
            
            // 按指标名称分组
            fluxQuery.append("|> group(columns: [\"metricName\"]) ");
            
            // 添加聚合函数
            String aggregateFunction = StringUtils.hasText(queryDTO.getAggregateFunction()) ? 
                    queryDTO.getAggregateFunction() : "mean";
            String window = StringUtils.hasText(queryDTO.getWindow()) ? 
                    queryDTO.getWindow() : "1h";
            
            fluxQuery.append("|> aggregateWindow(every: ").append(window).append(", ");
            fluxQuery.append("fn: ").append(aggregateFunction).append(", createEmpty: false) ");
            
            // 添加分页
            if (queryDTO.getLimit() > 0) {
                fluxQuery.append("|> limit(n: ").append(queryDTO.getLimit()).append(")");
            }
            
            log.debug("Generated Flux query for group by: {}", fluxQuery);
            
            QueryApi queryApi = influxDBClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(fluxQuery.toString(), organization);
            
            List<Object> resultList = new ArrayList<>();
            for (FluxTable table : tables) {
                Map<String, Object> groupResult = new HashMap<>();
                if (!table.getRecords().isEmpty()) {
                    FluxRecord record = table.getRecords().get(0);
                    groupResult.put("metricName", record.getValueByKey("metricName"));
                    groupResult.put("count", table.getRecords().size());
                    
                    double sum = 0;
                    for (FluxRecord r : table.getRecords()) {
                        sum += Double.parseDouble(r.getValue().toString());
                    }
                    groupResult.put("sum", sum);
                    groupResult.put("average", sum / table.getRecords().size());
                    
                    resultList.add(groupResult);
                }
            }
            
            return resultList;
        } catch (Exception e) {
            log.error("分组查询时间序列数据失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 将DTO转换为实体
     */
    private TimeSeriesData convertToEntity(TimeSeriesDataDTO dto) {
        TimeSeriesData entity = new TimeSeriesData();
        BeanUtils.copyProperties(dto, entity);
        
        // 如果没有设置时间，则使用当前时间
        if (entity.getTime() == null) {
            entity.setTime(Instant.now());
        }
        
        return entity;
    }
} 