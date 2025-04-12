package com.skyi.collector.repository;

import com.skyi.collector.model.CollectorTaskSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 采集任务调度Repository
 */
public interface CollectorTaskScheduleRepository extends JpaRepository<CollectorTaskSchedule, Long>, JpaSpecificationExecutor<CollectorTaskSchedule> {
    
    /**
     * 根据任务ID查询调度配置
     *
     * @param taskId 任务ID
     * @return 调度配置列表
     */
    List<CollectorTaskSchedule> findByTaskId(Long taskId);
    
    /**
     * 根据任务ID和启用状态查询调度配置
     *
     * @param taskId 任务ID
     * @param enabled 启用状态
     * @return 调度配置列表
     */
    List<CollectorTaskSchedule> findByTaskIdAndEnabled(Long taskId, Integer enabled);
    
    /**
     * 根据启用状态查询调度配置
     *
     * @param enabled 启用状态
     * @return 调度配置列表
     */
    List<CollectorTaskSchedule> findByEnabled(Integer enabled);
    
    /**
     * 查询需要执行的固定频率调度配置
     *
     * @param currentTime 当前时间
     * @return 需要执行的调度配置列表
     */
    @Query("SELECT s FROM CollectorTaskSchedule s WHERE s.enabled = 1 AND s.scheduleType = 1 " +
            "AND (s.lastExecuteTime IS NULL OR s.nextExecuteTime <= :currentTime) " +
            "AND (s.startTime IS NULL OR s.startTime <= :currentTime) " +
            "AND (s.endTime IS NULL OR s.endTime >= :currentTime)")
    List<CollectorTaskSchedule> findFixedRateSchedulesToExecute(LocalDateTime currentTime);
    
    /**
     * 查询需要执行的一次性任务调度配置
     *
     * @param currentTime 当前时间
     * @return 需要执行的调度配置列表
     */
    @Query("SELECT s FROM CollectorTaskSchedule s WHERE s.enabled = 1 AND s.scheduleType = 3 " +
            "AND s.executeTime <= :currentTime AND s.lastExecuteTime IS NULL")
    List<CollectorTaskSchedule> findOneTimeSchedulesToExecute(LocalDateTime currentTime);
    
    /**
     * 查询当前有效的CRON调度配置
     *
     * @param currentTime 当前时间
     * @return 当前有效的CRON调度配置
     */
    @Query("SELECT s FROM CollectorTaskSchedule s WHERE s.enabled = 1 AND s.scheduleType = 2 " +
            "AND (s.startTime IS NULL OR s.startTime <= :currentTime) " +
            "AND (s.endTime IS NULL OR s.endTime >= :currentTime)")
    List<CollectorTaskSchedule> findValidCronSchedules(LocalDateTime currentTime);
} 