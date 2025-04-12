package com.skyi.collector.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyi.collector.dto.MetricDataDTO;
import com.skyi.collector.model.CollectorTask;
import com.skyi.collector.model.CollectorTaskInstance;
import com.skyi.collector.repository.CollectorMetricDataRepository;
import com.skyi.collector.repository.CollectorTaskInstanceRepository;
import com.skyi.collector.repository.CollectorTaskRepository;
import com.skyi.collector.service.CollectorTaskExecutor;
import com.skyi.collector.service.collector.Collector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 采集任务执行器实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorTaskExecutorImpl implements CollectorTaskExecutor {
    
    private final List<Collector> collectors;
    private final CollectorTaskRepository taskRepository;
    private final CollectorTaskInstanceRepository instanceRepository;
    private final CollectorMetricDataRepository metricDataRepository;
    private final ObjectMapper objectMapper;
    
    // 线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    @Override
    @Transactional
    public Long executeTask(CollectorTask task) {
        // 创建任务实例
        CollectorTaskInstance instance = new CollectorTaskInstance();
        instance.setTaskId(task.getId());
        instance.setAssetId(task.getAssetId());
        instance.setStartTime(LocalDateTime.now());
        instance.setStatus(2); // 进行中
        
        // 保存实例
        CollectorTaskInstance savedInstance = instanceRepository.save(instance);
        
        try {
            // 查找适合的采集器
            Collector collector = findSuitableCollector(task);
            if (collector == null) {
                log.error("找不到适合的采集器, taskId={}, type={}, protocol={}", 
                        task.getId(), task.getType(), task.getProtocol());
                
                // 更新实例状态为失败
                updateInstanceStatus(savedInstance.getId(), false, "找不到适合的采集器");
                return savedInstance.getId();
            }
            
            // 执行采集
            List<MetricDataDTO> metricDataList = collector.collect(task, savedInstance.getId());
            
            // 保存采集数据
            saveMetricData(metricDataList);
            
            // 更新实例状态为成功
            updateInstanceStatus(savedInstance.getId(), true, null);
            
            // 更新任务最后执行时间和状态
            task.setLastExecuteTime(LocalDateTime.now());
            task.setLastExecuteStatus(1);
            taskRepository.save(task);
            
            return savedInstance.getId();
            
        } catch (Exception e) {
            log.error("执行采集任务出错: {}, taskId={}", e.getMessage(), task.getId(), e);
            
            // 更新实例状态为失败
            updateInstanceStatus(savedInstance.getId(), false, e.getMessage());
            
            // 更新任务最后执行时间和状态
            task.setLastExecuteTime(LocalDateTime.now());
            task.setLastExecuteStatus(0);
            taskRepository.save(task);
            
            return savedInstance.getId();
        }
    }
    
    @Override
    public void executeScheduledTask() {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        
        // 查询需要执行的任务
        List<CollectorTask> tasks = taskRepository.findTasksNeedExecute(now);
        log.info("定时执行任务数量: {}", tasks.size());
        
        // 异步执行每个任务
        for (CollectorTask task : tasks) {
            scheduleTask(task, false);
        }
    }
    
    @Override
    public Long scheduleTask(CollectorTask task, boolean block) {
        if (block) {
            // 同步执行
            return executeTask(task);
        } else {
            // 异步执行
            CompletableFuture.runAsync(() -> {
                executeTask(task);
            }, executorService);
            
            return null;
        }
    }
    
    /**
     * 查找适合的采集器
     * 
     * @param task 采集任务
     * @return 采集器
     */
    private Collector findSuitableCollector(CollectorTask task) {
        for (Collector collector : collectors) {
            if (collector.supports(task)) {
                return collector;
            }
        }
        return null;
    }
    
    /**
     * 更新任务实例状态
     * 
     * @param instanceId 实例ID
     * @param success 是否成功
     * @param errorMessage 错误信息
     */
    private void updateInstanceStatus(Long instanceId, boolean success, String errorMessage) {
        CollectorTaskInstance instance = instanceRepository.findById(instanceId).orElse(null);
        if (instance != null) {
            instance.setEndTime(LocalDateTime.now());
            instance.setStatus(success ? 1 : 0);
            instance.setErrorMessage(errorMessage);
            instanceRepository.save(instance);
        }
    }
    
    /**
     * 保存指标数据
     * 
     * @param metricDataList 指标数据列表
     */
    private void saveMetricData(List<MetricDataDTO> metricDataList) {
        for (MetricDataDTO metricDataDTO : metricDataList) {
            try {
                // 转换DTO到实体
                com.skyi.collector.model.CollectorMetricData metricData = new com.skyi.collector.model.CollectorMetricData();
                metricData.setTaskId(metricDataDTO.getTaskId());
                metricData.setInstanceId(metricDataDTO.getInstanceId());
                metricData.setAssetId(metricDataDTO.getAssetId());
                metricData.setMetricName(metricDataDTO.getMetricName());
                metricData.setMetricValue(metricDataDTO.getMetricValue());
                metricData.setMetricLabels(objectMapper.writeValueAsString(metricDataDTO.getMetricLabels()));
                metricData.setCollectTime(metricDataDTO.getCollectTime());
                
                // 保存数据
                metricDataRepository.save(metricData);
            } catch (Exception e) {
                log.error("保存指标数据出错: {}", e.getMessage(), e);
            }
        }
    }
}