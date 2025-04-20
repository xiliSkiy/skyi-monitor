package com.skyi.storage.service;

import com.skyi.storage.dto.TimeSeriesDataDTO;
import com.skyi.storage.dto.TimeSeriesQueryDTO;
import com.skyi.storage.model.TimeSeriesData;

import java.util.List;

/**
 * 时间序列服务接口
 */
public interface TimeSeriesService {

    /**
     * 保存单条时间序列数据
     *
     * @param dataDTO 时间序列数据DTO
     * @return 是否保存成功
     */
    boolean saveData(TimeSeriesDataDTO dataDTO);

    /**
     * 批量保存时间序列数据
     *
     * @param dataDTOList 时间序列数据DTO列表
     * @return 是否保存成功
     */
    boolean saveBatchData(List<TimeSeriesDataDTO> dataDTOList);

    /**
     * 查询时间序列数据
     *
     * @param queryDTO 查询条件
     * @return 时间序列数据列表
     */
    List<TimeSeriesData> queryData(TimeSeriesQueryDTO queryDTO);

    /**
     * 删除时间序列数据
     *
     * @param assetId    资产ID
     * @param metricName 指标名称
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 是否删除成功
     */
    boolean deleteData(String assetId, String metricName, String startTime, String endTime);

    /**
     * 查询最新的时间序列数据
     *
     * @param assetId    资产ID
     * @param metricName 指标名称
     * @return 最新的时间序列数据
     */
    TimeSeriesData queryLatestData(String assetId, String metricName);

    /**
     * 根据指标名称分组统计数据
     *
     * @param queryDTO 查询条件
     * @return 分组统计结果
     */
    List<Object> groupByMetric(TimeSeriesQueryDTO queryDTO);
} 