package com.skyi.collector.service;

import com.skyi.collector.dto.CollectorTaskDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 采集任务服务接口
 */
public interface CollectorTaskService {
    
    /**
     * 创建采集任务
     *
     * @param taskDTO 任务信息
     * @return 创建后的任务信息
     */
    CollectorTaskDTO createTask(CollectorTaskDTO taskDTO);
    
    /**
     * 更新采集任务
     *
     * @param id 任务ID
     * @param taskDTO 任务信息
     * @return 更新后的任务信息
     */
    CollectorTaskDTO updateTask(Long id, CollectorTaskDTO taskDTO);
    
    /**
     * 删除采集任务
     *
     * @param id 任务ID
     */
    void deleteTask(Long id);
    
    /**
     * 根据ID查询采集任务
     *
     * @param id 任务ID
     * @return 任务信息
     */
    CollectorTaskDTO getTaskById(Long id);
    
    /**
     * 根据编码查询采集任务
     *
     * @param code 任务编码
     * @return 任务信息
     */
    CollectorTaskDTO getTaskByCode(String code);
    
    /**
     * 分页查询采集任务列表
     *
     * @param name 任务名称
     * @param type 任务类型
     * @param status 状态
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    Page<CollectorTaskDTO> listTasks(String name, String type, Integer status, Pageable pageable);
    
    /**
     * 根据类型查询采集任务列表
     *
     * @param type 任务类型
     * @return 任务列表
     */
    List<CollectorTaskDTO> listTasksByType(String type);
    
    /**
     * 根据资产ID查询采集任务列表
     *
     * @param assetId 资产ID
     * @return 任务列表
     */
    List<CollectorTaskDTO> listTasksByAssetId(Long assetId);
    
    /**
     * 根据协议查询采集任务列表
     *
     * @param protocol 协议类型
     * @return 任务列表
     */
    List<CollectorTaskDTO> listTasksByProtocol(String protocol);
    
    /**
     * 启动采集任务
     *
     * @param id 任务ID
     * @return 更新后的任务信息
     */
    CollectorTaskDTO startTask(Long id);
    
    /**
     * 停止采集任务
     *
     * @param id 任务ID
     * @return 更新后的任务信息
     */
    CollectorTaskDTO stopTask(Long id);
    
    /**
     * 立即执行采集任务
     *
     * @param id 任务ID
     * @return 任务实例ID
     */
    Long executeTaskNow(Long id);
} 