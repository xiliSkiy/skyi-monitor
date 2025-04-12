package com.skyi.collector.job;

import com.skyi.collector.service.CollectorTaskExecutor;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 采集任务调度器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CollectorTaskScheduler {
    
    private final CollectorTaskExecutor collectorTaskExecutor;
    
    /**
     * 定时检查并执行到期的采集任务
     * 每10秒执行一次
     */
    @Scheduled(fixedRate = 10000)
    public void scheduleCollectorTasks() {
        log.debug("开始执行定时采集任务");
        try {
            collectorTaskExecutor.executeScheduledTask();
        } catch (Exception e) {
            log.error("执行定时采集任务出错: {}", e.getMessage(), e);
        }
    }
    
    /**
     * XXL-JOB调度的采集任务
     * 采集任务调度
     */
    @XxlJob("collectorTaskJobHandler")
    public void executeCollectorTaskJob() {
        log.debug("开始执行XXL-JOB采集任务");
        try {
            collectorTaskExecutor.executeScheduledTask();
        } catch (Exception e) {
            log.error("执行XXL-JOB采集任务出错: {}", e.getMessage(), e);
        }
    }
} 