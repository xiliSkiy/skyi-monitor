package com.skyi.visualization.service.impl;

import com.skyi.visualization.dto.MetricDataDTO;
import com.skyi.visualization.dto.MetricDataDTO.DataPoint;
import com.skyi.visualization.dto.MetricQueryDTO;
import com.skyi.visualization.service.MetricService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 指标查询服务实现类
 */
@Service
@Slf4j
public class MetricServiceImpl implements MetricService {

    private final RestTemplate restTemplate;
    private final InfluxDBMetricAdapter influxDBAdapter;
    
    @Value("${skyi.tsdb.url}")
    private String tsdbUrl;
    
    @Value("${skyi.tsdb.api.query:/api/v1/timeseries/query}")
    private String queryApi;
    
    @Value("${skyi.tsdb.api.metadata:/api/v1/metadata}")
    private String metadataApi;
    
    @Autowired
    public MetricServiceImpl(RestTemplate restTemplate, InfluxDBMetricAdapter influxDBAdapter) {
        this.restTemplate = restTemplate;
        this.influxDBAdapter = influxDBAdapter;
    }

    @Override
    public MetricDataDTO queryMetricData(MetricQueryDTO queryDTO) {
        try {
            // 构建查询参数
            Map<String, Object> requestBody = buildQueryParams(queryDTO);
            
            // 使用InfluxDB适配器执行查询
            Map<String, Object> responseData = influxDBAdapter.queryData(requestBody);
            
            return parseMetricDataResponse(responseData, queryDTO);
        } catch (Exception e) {
            log.error("查询指标数据失败: {}", queryDTO.getName(), e);
            return new MetricDataDTO();
        }
    }
    
    @Override
    public List<MetricDataDTO> queryMultiMetricData(List<MetricQueryDTO> queryDTOList) {
        return queryDTOList.stream()
                .map(this::queryMetricData)
                .collect(Collectors.toList());
    }
    
    @Override
    public Double queryLatestValue(String metricName, Map<String, String> tags) {
        MetricQueryDTO queryDTO = new MetricQueryDTO();
        queryDTO.setName(metricName);
        queryDTO.setTags(tags);
        queryDTO.setLimit(1);
        queryDTO.setOrder("desc");
        
        MetricDataDTO data = queryMetricData(queryDTO);
        if (data != null && data.getDataPoints() != null && !data.getDataPoints().isEmpty()) {
            DataPoint dataPoint = data.getDataPoints().get(0);
            return dataPoint.getValue();
        }
        
        return null;
    }
    
    @Override
    public Double queryAggregateValue(String metricName, Map<String, String> tags, 
                                     Instant startTime, Instant endTime, 
                                     String aggregateFunction) {
        MetricQueryDTO queryDTO = new MetricQueryDTO();
        queryDTO.setName(metricName);
        queryDTO.setTags(tags);
        queryDTO.setStartTime(startTime);
        queryDTO.setEndTime(endTime);
        queryDTO.setAggregateFunction(aggregateFunction);
        
        MetricDataDTO data = queryMetricData(queryDTO);
        if (data != null && data.getDataPoints() != null && !data.getDataPoints().isEmpty()) {
            // 如果是聚合查询，应该只有一个值
            DataPoint dataPoint = data.getDataPoints().get(0);
            return dataPoint.getValue();
        }
        
        return null;
    }
    
    @Override
    public List<String> listMetricNames() {
        try {
            // 使用InfluxDB适配器获取指标名称列表
            return influxDBAdapter.getMetricNames();
        } catch (Exception e) {
            log.error("获取指标名称列表失败", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<String> listTagKeys(String metricName) {
        try {
            // 使用InfluxDB适配器获取标签键列表
            return influxDBAdapter.getTagKeys(metricName);
        } catch (Exception e) {
            log.error("获取指标标签键列表失败: {}", metricName, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<String> listTagValues(String metricName, String tagKey) {
        try {
            // 使用InfluxDB适配器获取标签值列表
            return influxDBAdapter.getTagValues(metricName, tagKey);
        } catch (Exception e) {
            log.error("获取指标标签值列表失败: {}, {}", metricName, tagKey, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 构建查询参数
     *
     * @param queryDTO 查询DTO
     * @return 查询参数Map
     */
    private Map<String, Object> buildQueryParams(MetricQueryDTO queryDTO) {
        Map<String, Object> params = new HashMap<>();
        params.put("metric", queryDTO.getName());
        
        if (queryDTO.getTags() != null && !queryDTO.getTags().isEmpty()) {
            params.put("tags", queryDTO.getTags());
        }
        
        if (queryDTO.getStartTime() != null) {
            params.put("start_time", queryDTO.getStartTime().toEpochMilli());
        }
        
        if (queryDTO.getEndTime() != null) {
            params.put("end_time", queryDTO.getEndTime().toEpochMilli());
        }
        
        if (queryDTO.getAggregateFunction() != null) {
            params.put("aggregateFunction", queryDTO.getAggregateFunction());
        }
        
        if (queryDTO.getInterval() != null) {
            params.put("interval", queryDTO.getInterval());
        }
        
        if (queryDTO.getLimit() != null) {
            params.put("limit", queryDTO.getLimit());
        }
        
        if (queryDTO.getOrder() != null) {
            params.put("order", queryDTO.getOrder());
        }
        
        if (queryDTO.getFill() != null) {
            params.put("fill", queryDTO.getFill());
        }
        
        return params;
    }
    
    /**
     * 解析响应数据
     *
     * @param responseData 响应数据
     * @param queryDTO 查询DTO
     * @return 指标数据DTO
     */
    @SuppressWarnings("unchecked")
    private MetricDataDTO parseMetricDataResponse(Map responseData, MetricQueryDTO queryDTO) {
        if (responseData == null) {
            return new MetricDataDTO();
        }
        
        MetricDataDTO metricData = new MetricDataDTO();
        metricData.setName(queryDTO.getName());
        metricData.setTags(queryDTO.getTags());
        
        Map<String, Object> dataMap = (Map<String, Object>) responseData.get("data");
        if (dataMap == null) {
            return metricData;
        }
        
        // 解析时间序列数据点
        List<DataPoint> dataPoints = new ArrayList<>();
        List<List<Object>> points = (List<List<Object>>) dataMap.get("datapoints");
        
        if (points != null) {
            for (List<Object> point : points) {
                if (point.size() >= 2) {
                    Long timestamp = ((Number) point.get(0)).longValue();
                    Double value = ((Number) point.get(1)).doubleValue();
                    DataPoint dataPoint = DataPoint.builder()
                        .timestamp(Instant.ofEpochMilli(timestamp))
                        .value(value)
                        .build();
                    dataPoints.add(dataPoint);
                }
            }
        }
        
        metricData.setDataPoints(dataPoints);
        
        // 设置聚合值
        if (dataMap.containsKey("aggregate")) {
            metricData.setAggregateValue(((Number) dataMap.get("aggregate")).doubleValue());
        }
        
        return metricData;
    }
} 