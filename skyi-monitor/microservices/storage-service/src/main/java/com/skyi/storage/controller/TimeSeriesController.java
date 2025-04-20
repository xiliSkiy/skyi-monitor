package com.skyi.storage.controller;

import com.skyi.storage.dto.TimeSeriesDataDTO;
import com.skyi.storage.dto.TimeSeriesQueryDTO;
import com.skyi.storage.model.TimeSeriesData;
import com.skyi.storage.service.TimeSeriesService;
import com.skyi.storage.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 时间序列数据控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/timeseries")
@Tag(name = "时间序列数据接口", description = "提供时间序列数据的存储和查询功能")
public class TimeSeriesController {

    @Autowired
    private TimeSeriesService timeSeriesService;

    @PostMapping("/data")
    @Operation(summary = "保存时间序列数据", description = "保存单条时间序列数据")
    public Result<Boolean> saveData(@Valid @RequestBody TimeSeriesDataDTO dataDTO) {
        log.debug("保存时间序列数据: {}", dataDTO);
        boolean result = timeSeriesService.saveData(dataDTO);
        return Result.success(result);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量保存时间序列数据", description = "批量保存多条时间序列数据")
    public Result<Boolean> saveBatchData(@Valid @RequestBody List<TimeSeriesDataDTO> dataDTOList) {
        log.debug("批量保存时间序列数据, 数量: {}", dataDTOList.size());
        boolean result = timeSeriesService.saveBatchData(dataDTOList);
        return Result.success(result);
    }

    @PostMapping("/query")
    @Operation(summary = "查询时间序列数据", description = "根据条件查询时间序列数据")
    public Result<List<TimeSeriesData>> queryData(@Valid @RequestBody TimeSeriesQueryDTO queryDTO) {
        log.debug("查询时间序列数据: {}", queryDTO);
        List<TimeSeriesData> dataList = timeSeriesService.queryData(queryDTO);
        return Result.success(dataList);
    }

    @GetMapping("/latest")
    @Operation(summary = "查询最新数据", description = "查询指定资产和指标的最新数据")
    public Result<TimeSeriesData> queryLatestData(
            @Parameter(description = "资产ID") @RequestParam String assetId,
            @Parameter(description = "指标名称") @RequestParam String metricName) {
        log.debug("查询最新数据, assetId: {}, metricName: {}", assetId, metricName);
        TimeSeriesData data = timeSeriesService.queryLatestData(assetId, metricName);
        return Result.success(data);
    }

    @PostMapping("/group")
    @Operation(summary = "分组统计数据", description = "根据指标名称分组统计数据")
    public Result<List<Object>> groupByMetric(@Valid @RequestBody TimeSeriesQueryDTO queryDTO) {
        log.debug("分组统计数据: {}", queryDTO);
        List<Object> resultList = timeSeriesService.groupByMetric(queryDTO);
        return Result.success(resultList);
    }

    @DeleteMapping("/data")
    @Operation(summary = "删除时间序列数据", description = "删除指定条件的时间序列数据")
    public Result<Boolean> deleteData(
            @Parameter(description = "资产ID") @RequestParam(required = false) String assetId,
            @Parameter(description = "指标名称") @RequestParam(required = false) String metricName,
            @Parameter(description = "开始时间") @RequestParam String startTime,
            @Parameter(description = "结束时间") @RequestParam String endTime) {
        log.debug("删除时间序列数据, assetId: {}, metricName: {}, startTime: {}, endTime: {}",
                assetId, metricName, startTime, endTime);
        boolean result = timeSeriesService.deleteData(assetId, metricName, startTime, endTime);
        return Result.success(result);
    }
} 