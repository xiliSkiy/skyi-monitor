package com.skyi.collector.controller;

import com.skyi.collector.dto.CollectorTaskScheduleDTO;
import com.skyi.collector.service.CollectorTaskScheduleService;
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
 * 采集任务调度控制器
 */
@Slf4j
@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class CollectorTaskScheduleController {
    
    private final CollectorTaskScheduleService scheduleService;
    
    /**
     * 创建任务调度
     *
     * @param scheduleDTO 调度信息
     * @return 创建结果
     */
    @PostMapping
    public Result<CollectorTaskScheduleDTO> createSchedule(@RequestBody @Validated CollectorTaskScheduleDTO scheduleDTO) {
        log.info("创建任务调度请求：{}", scheduleDTO.getName());
        CollectorTaskScheduleDTO created = scheduleService.createSchedule(scheduleDTO);
        return Result.success(created);
    }
    
    /**
     * 更新任务调度
     *
     * @param id 调度ID
     * @param scheduleDTO 调度信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<CollectorTaskScheduleDTO> updateSchedule(@PathVariable Long id, 
                                                          @RequestBody @Validated CollectorTaskScheduleDTO scheduleDTO) {
        log.info("更新任务调度请求：id={}", id);
        CollectorTaskScheduleDTO updated = scheduleService.updateSchedule(id, scheduleDTO);
        return Result.success(updated);
    }
    
    /**
     * 删除任务调度
     *
     * @param id 调度ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteSchedule(@PathVariable Long id) {
        log.info("删除任务调度请求：id={}", id);
        scheduleService.deleteSchedule(id);
        return Result.success();
    }
    
    /**
     * 查询任务调度
     *
     * @param id 调度ID
     * @return 调度信息
     */
    @GetMapping("/{id}")
    public Result<CollectorTaskScheduleDTO> getScheduleById(@PathVariable Long id) {
        log.info("查询任务调度请求：id={}", id);
        CollectorTaskScheduleDTO schedule = scheduleService.getScheduleById(id);
        return Result.success(schedule);
    }
    
    /**
     * 分页查询调度列表
     *
     * @param name 调度名称
     * @param taskId 任务ID
     * @param enabled 是否启用
     * @param page 页码
     * @param size 每页大小
     * @return 调度分页列表
     */
    @GetMapping
    public Result<Page<CollectorTaskScheduleDTO>> listSchedules(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Integer enabled,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("分页查询任务调度请求：name={}, taskId={}, enabled={}, page={}, size={}", 
                name, taskId, enabled, page, size);
        
        // 兼容前端页码从0或1开始的情况
        int pageIndex = page;
        if (page > 0) {
            pageIndex = page - 1;
        }
        
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<CollectorTaskScheduleDTO> schedulePage = scheduleService.listSchedules(name, taskId, enabled, pageable);
        return Result.success(schedulePage);
    }
    
    /**
     * 根据任务ID查询调度列表
     *
     * @param taskId 任务ID
     * @return 调度列表
     */
    @GetMapping("/task/{taskId}")
    public Result<List<CollectorTaskScheduleDTO>> listSchedulesByTaskId(@PathVariable Long taskId) {
        log.info("查询任务调度请求：taskId={}", taskId);
        List<CollectorTaskScheduleDTO> schedules = scheduleService.listSchedulesByTaskId(taskId);
        return Result.success(schedules);
    }
    
    /**
     * 启用任务调度
     *
     * @param id 调度ID
     * @return 更新结果
     */
    @PutMapping("/{id}/enable")
    public Result<CollectorTaskScheduleDTO> enableSchedule(@PathVariable Long id) {
        log.info("启用任务调度请求：id={}", id);
        CollectorTaskScheduleDTO updated = scheduleService.enableSchedule(id);
        return Result.success(updated);
    }
    
    /**
     * 禁用任务调度
     *
     * @param id 调度ID
     * @return 更新结果
     */
    @PutMapping("/{id}/disable")
    public Result<CollectorTaskScheduleDTO> disableSchedule(@PathVariable Long id) {
        log.info("禁用任务调度请求：id={}", id);
        CollectorTaskScheduleDTO updated = scheduleService.disableSchedule(id);
        return Result.success(updated);
    }
    
    /**
     * 立即执行调度
     *
     * @param id 调度ID
     * @return 执行结果
     */
    @PostMapping("/{id}/execute")
    public Result<Long> executeScheduleNow(@PathVariable Long id) {
        log.info("立即执行任务调度请求：id={}", id);
        Long instanceId = scheduleService.executeScheduleNow(id);
        return Result.success(instanceId);
    }
} 