package com.skyi.collector.controller;

import com.skyi.collector.dto.CollectorTaskDTO;
import com.skyi.collector.service.CollectorTaskService;
import com.skyi.common.model.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 采集任务控制器
 */
@Slf4j
@RestController
@RequestMapping("/collector/tasks")
@RequiredArgsConstructor
public class CollectorTaskController {
    
    private final CollectorTaskService taskService;
    
    /**
     * 创建采集任务
     *
     * @param taskDTO 任务信息
     * @return 创建结果
     */
    @PostMapping
    public Result<CollectorTaskDTO> createTask(@RequestBody @Validated CollectorTaskDTO taskDTO) {
        log.info("创建采集任务请求：{}", taskDTO.getName());
        CollectorTaskDTO created = taskService.createTask(taskDTO);
        return Result.success(created);
    }
    
    /**
     * 更新采集任务
     *
     * @param id 任务ID
     * @param taskDTO 任务信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<CollectorTaskDTO> updateTask(@PathVariable Long id, 
                                           @RequestBody @Validated CollectorTaskDTO taskDTO) {
        log.info("更新采集任务请求：id={}", id);
        CollectorTaskDTO updated = taskService.updateTask(id, taskDTO);
        return Result.success(updated);
    }
    
    /**
     * 删除采集任务
     *
     * @param id 任务ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTask(@PathVariable Long id) {
        log.info("删除采集任务请求：id={}", id);
        taskService.deleteTask(id);
        return Result.success();
    }
    
    /**
     * 查询采集任务
     *
     * @param id 任务ID
     * @return 任务信息
     */
    @GetMapping("/{id}")
    public Result<CollectorTaskDTO> getTaskById(@PathVariable Long id) {
        log.info("查询采集任务请求：id={}", id);
        CollectorTaskDTO task = taskService.getTaskById(id);
        return Result.success(task);
    }
    
    /**
     * 分页查询任务列表
     *
     * @param name 任务名称
     * @param type 任务类型
     * @param status 状态
     * @param page 页码
     * @param size 每页大小
     * @return 任务分页列表
     */
    @GetMapping
    public Result<Page<CollectorTaskDTO>> listTasks(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("分页查询采集任务请求：name={}, type={}, status={}, page={}, size={}", 
                name, type, status, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<CollectorTaskDTO> taskPage = taskService.listTasks(name, type, status, pageable);
        return Result.success(taskPage);
    }
    
    /**
     * 根据类型查询任务列表
     *
     * @param type 任务类型
     * @return 任务列表
     */
    @GetMapping("/type/{type}")
    public Result<List<CollectorTaskDTO>> listTasksByType(@PathVariable String type) {
        log.info("根据类型查询采集任务请求：type={}", type);
        List<CollectorTaskDTO> tasks = taskService.listTasksByType(type);
        return Result.success(tasks);
    }
    
    /**
     * 根据资产ID查询任务列表
     *
     * @param assetId 资产ID
     * @return 任务列表
     */
    @GetMapping("/asset/{assetId}")
    public Result<List<CollectorTaskDTO>> listTasksByAssetId(@PathVariable Long assetId) {
        log.info("根据资产ID查询采集任务请求：assetId={}", assetId);
        List<CollectorTaskDTO> tasks = taskService.listTasksByAssetId(assetId);
        return Result.success(tasks);
    }
    
    /**
     * 启动采集任务
     *
     * @param id 任务ID
     * @return 更新结果
     */
    @PutMapping("/{id}/start")
    public Result<CollectorTaskDTO> startTask(@PathVariable Long id) {
        log.info("启动采集任务请求：id={}", id);
        CollectorTaskDTO updated = taskService.startTask(id);
        return Result.success(updated);
    }
    
    /**
     * 停止采集任务
     *
     * @param id 任务ID
     * @return 更新结果
     */
    @PutMapping("/{id}/stop")
    public Result<CollectorTaskDTO> stopTask(@PathVariable Long id) {
        log.info("停止采集任务请求：id={}", id);
        CollectorTaskDTO updated = taskService.stopTask(id);
        return Result.success(updated);
    }
    
    /**
     * 立即执行采集任务
     *
     * @param id 任务ID
     * @return 任务实例ID
     */
    @PostMapping("/{id}/execute")
    public Result<Long> executeTaskNow(@PathVariable Long id) {
        log.info("立即执行采集任务请求：id={}", id);
        Long instanceId = taskService.executeTaskNow(id);
        return Result.success(instanceId);
    }
} 