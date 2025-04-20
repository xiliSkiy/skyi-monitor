package com.skyi.collector.service;

import com.skyi.collector.dto.CollectorTaskInstanceDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 采集任务实例服务接口
 */
public interface CollectorTaskInstanceService {

    /**
     * 获取任务实例详情
     *
     * @param id 实例ID
     * @return 实例详情
     */
    CollectorTaskInstanceDTO getInstanceById(Long id);

    /**
     * 分页查询任务实例列表
     *
     * @param taskId 任务ID
     * @param status 执行状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 实例分页列表
     */
    Page<CollectorTaskInstanceDTO> listInstances(Long taskId, Integer status, 
                                        String startTime, String endTime, Pageable pageable);

    /**
     * 获取任务最近一次执行实例
     *
     * @param taskId 任务ID
     * @return 最近一次执行实例
     */
    CollectorTaskInstanceDTO getLatestInstance(Long taskId);
} 