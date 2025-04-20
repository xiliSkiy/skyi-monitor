package com.skyi.collector.repository;

import com.skyi.collector.model.CollectorMetricData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 采集指标数据访问接口
 */
public interface CollectorMetricDataRepository extends JpaRepository<CollectorMetricData, Long>, JpaSpecificationExecutor<CollectorMetricData> {
    
    /**
     * 根据任务ID查询指标数据
     *
     * @param taskId 任务ID
     * @return 指标数据列表
     */
    List<CollectorMetricData> findByTaskId(Long taskId);
    
    /**
     * 根据任务实例ID查询指标数据
     *
     * @param instanceId 任务实例ID
     * @return 指标数据列表
     */
    List<CollectorMetricData> findByInstanceId(Long instanceId);
    
    /**
     * 根据资产ID查询指标数据
     *
     * @param assetId 资产ID
     * @return 指标数据列表
     */
    List<CollectorMetricData> findByAssetId(Long assetId);
    
    /**
     * 根据指标名称查询指标数据
     *
     * @param metricName 指标名称
     * @return 指标数据列表
     */
    List<CollectorMetricData> findByMetricName(String metricName);
    
    /**
     * 根据资产ID和指标名称查询指标数据
     *
     * @param assetId 资产ID
     * @param metricName 指标名称
     * @return 指标数据列表
     */
    List<CollectorMetricData> findByAssetIdAndMetricName(Long assetId, String metricName);
    
    /**
     * 根据采集时间范围查询指标数据
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 指标数据列表
     */
    List<CollectorMetricData> findByCollectTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据资产ID、指标名称和采集时间范围查询指标数据
     *
     * @param assetId 资产ID
     * @param metricName 指标名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 指标数据列表
     */
    List<CollectorMetricData> findByAssetIdAndMetricNameAndCollectTimeBetween(
            Long assetId, String metricName, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据实例ID统计指标数量
     *
     * @param instanceId 实例ID
     * @return 指标数量
     */
    long countByInstanceId(Long instanceId);
} 