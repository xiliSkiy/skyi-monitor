package com.skyi.collector.repository;

import com.skyi.collector.model.CollectorTaskInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 采集任务实例数据访问接口
 */
public interface CollectorTaskInstanceRepository extends JpaRepository<CollectorTaskInstance, Long>, JpaSpecificationExecutor<CollectorTaskInstance> {
    
    /**
     * 根据任务ID查询任务实例列表
     *
     * @param taskId 任务ID
     * @return 任务实例列表
     */
    List<CollectorTaskInstance> findByTaskId(Long taskId);
    
    /**
     * 根据资产ID查询任务实例列表
     *
     * @param assetId 资产ID
     * @return 任务实例列表
     */
    List<CollectorTaskInstance> findByAssetId(Long assetId);
    
    /**
     * 根据状态查询任务实例列表
     *
     * @param status 状态
     * @return 任务实例列表
     */
    List<CollectorTaskInstance> findByStatus(Integer status);
    
    /**
     * 根据任务ID和状态查询任务实例列表
     *
     * @param taskId 任务ID
     * @param status 状态
     * @return 任务实例列表
     */
    List<CollectorTaskInstance> findByTaskIdAndStatus(Long taskId, Integer status);
    
    /**
     * 根据开始时间范围查询任务实例列表
     *
     * @param startTimeFrom 开始时间起点
     * @param startTimeTo 开始时间终点
     * @return 任务实例列表
     */
    List<CollectorTaskInstance> findByStartTimeBetween(LocalDateTime startTimeFrom, LocalDateTime startTimeTo);
} 