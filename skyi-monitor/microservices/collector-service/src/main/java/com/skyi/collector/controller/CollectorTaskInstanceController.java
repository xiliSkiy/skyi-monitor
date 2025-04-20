package com.skyi.collector.controller;

import com.skyi.collector.dto.CollectorTaskInstanceDTO;
import com.skyi.collector.service.CollectorTaskInstanceService;
import com.skyi.common.model.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 采集任务实例控制器
 */
@Slf4j
@RestController
@RequestMapping("/collector/instances")
@RequiredArgsConstructor
public class CollectorTaskInstanceController {
    
    private final CollectorTaskInstanceService instanceService;
    
    /**
     * 分页查询任务实例列表
     *
     * @param taskId 任务ID
     * @param status 执行状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 页码
     * @param size 每页大小
     * @return 任务实例分页列表
     */
    @GetMapping
    public Result<Page<CollectorTaskInstanceDTO>> listTaskInstances(
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("分页查询采集任务实例请求：taskId={}, status={}, page={}, size={}", 
                taskId, status, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));
        Page<CollectorTaskInstanceDTO> instancePage = 
                instanceService.listInstances(taskId, status, startTime, endTime, pageable);
        return Result.success(instancePage);
    }
    
    /**
     * 获取任务实例详情
     *
     * @param id 实例ID
     * @return 实例详情
     */
    @GetMapping("/{id}")
    public Result<CollectorTaskInstanceDTO> getInstanceById(@PathVariable Long id) {
        log.info("查询采集任务实例详情请求：id={}", id);
        CollectorTaskInstanceDTO instance = instanceService.getInstanceById(id);
        return Result.success(instance);
    }
    
    /**
     * 根据任务ID查询最近的执行实例
     *
     * @param taskId 任务ID
     * @return 最近的执行实例
     */
    @GetMapping("/latest")
    public Result<CollectorTaskInstanceDTO> getLatestInstance(
            @RequestParam Long taskId) {
        log.info("查询最近一次执行实例请求：taskId={}", taskId);
        CollectorTaskInstanceDTO instance = instanceService.getLatestInstance(taskId);
        return Result.success(instance);
    }
} 