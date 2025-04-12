package com.skyi.collector.service;

import com.skyi.collector.dto.CollectorTaskScheduleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 采集任务调度服务接口
 */
public interface CollectorTaskScheduleService {
    
    /**
     * 创建调度配置
     *
     * @param scheduleDTO 调度配置信息
     * @return 创建后的调度配置信息
     */
    CollectorTaskScheduleDTO createSchedule(CollectorTaskScheduleDTO scheduleDTO);
    
    /**
     * 更新调度配置
     *
     * @param id 调度配置ID
     * @param scheduleDTO 调度配置信息
     * @return 更新后的调度配置信息
     */
    CollectorTaskScheduleDTO updateSchedule(Long id, CollectorTaskScheduleDTO scheduleDTO);
    
    /**
     * 删除调度配置
     *
     * @param id 调度配置ID
     */
    void deleteSchedule(Long id);
    
    /**
     * 根据ID查询调度配置
     *
     * @param id 调度配置ID
     * @return 调度配置信息
     */
    CollectorTaskScheduleDTO getScheduleById(Long id);
    
    /**
     * 分页查询调度配置列表
     *
     * @param name 调度名称
     * @param taskId 任务ID
     * @param enabled 是否启用
     * @param pageable 分页参数
     * @return 调度配置分页列表
     */
    Page<CollectorTaskScheduleDTO> listSchedules(String name, Long taskId, Integer enabled, Pageable pageable);
    
    /**
     * 根据任务ID查询调度配置列表
     *
     * @param taskId 任务ID
     * @return 调度配置列表
     */
    List<CollectorTaskScheduleDTO> listSchedulesByTaskId(Long taskId);
    
    /**
     * 启用调度配置
     *
     * @param id 调度配置ID
     * @return 更新后的调度配置信息
     */
    CollectorTaskScheduleDTO enableSchedule(Long id);
    
    /**
     * 禁用调度配置
     *
     * @param id 调度配置ID
     * @return 更新后的调度配置信息
     */
    CollectorTaskScheduleDTO disableSchedule(Long id);
    
    /**
     * 立即执行调度
     *
     * @param id 调度配置ID
     * @return 任务实例ID
     */
    Long executeScheduleNow(Long id);
    
    /**
     * 计算下次执行时间
     *
     * @param scheduleDTO 调度配置信息
     * @return 下次执行时间
     */
    void calculateNextExecuteTime(CollectorTaskScheduleDTO scheduleDTO);
} 