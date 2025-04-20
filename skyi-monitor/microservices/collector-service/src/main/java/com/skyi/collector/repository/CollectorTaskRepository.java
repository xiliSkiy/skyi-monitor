package com.skyi.collector.repository;

import com.skyi.collector.model.CollectorTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 采集任务数据访问接口
 */
public interface CollectorTaskRepository extends JpaRepository<CollectorTask, Long>, JpaSpecificationExecutor<CollectorTask> {
    
    /**
     * 根据任务编码查询任务
     *
     * @param code 任务编码
     * @return 任务信息
     */
    Optional<CollectorTask> findByCode(String code);
    
    /**
     * 根据类型查询任务列表
     *
     * @param type 任务类型
     * @return 任务列表
     */
    List<CollectorTask> findByType(String type);
    
    /**
     * 根据资产ID查询任务列表
     *
     * @param assetId 资产ID
     * @return 任务列表
     */
    List<CollectorTask> findByAssetId(Long assetId);
    
    /**
     * 根据协议查询任务列表
     *
     * @param protocol 协议类型
     * @return 任务列表
     */
    List<CollectorTask> findByProtocol(String protocol);
    
    /**
     * 根据状态查询任务列表
     *
     * @param status 状态
     * @return 任务列表
     */
    List<CollectorTask> findByStatus(Integer status);
    
    /**
     * 查询需要执行的任务列表
     *
     * @param currentTime 当前时间
     * @return 需要执行的任务列表
     */
    @Query("SELECT t FROM CollectorTask t WHERE t.status = 1 AND (t.lastExecuteTime IS NULL OR TIMESTAMPDIFF(SECOND, t.lastExecuteTime, :currentTime) >= t.interval)")
    List<CollectorTask> findTasksNeedExecute(LocalDateTime currentTime);
} 