package com.skyi.collector.service;

import com.skyi.collector.model.CollectorTask;

/**
 * 采集任务执行器接口
 */
public interface CollectorTaskExecutor {
    
    /**
     * 执行采集任务
     *
     * @param task 采集任务
     * @return 任务实例ID
     */
    Long executeTask(CollectorTask task);
    
    /**
     * 执行定时任务
     */
    void executeScheduledTask();
    
    /**
     * 调度任务
     *
     * @param task 采集任务
     * @param block 是否阻塞等待结果
     * @return 任务实例ID
     */
    Long scheduleTask(CollectorTask task, boolean block);
} 