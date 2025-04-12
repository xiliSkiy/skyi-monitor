package com.skyi.collector.service.impl;

import com.skyi.collector.dto.CollectorTaskScheduleDTO;
import com.skyi.collector.model.CollectorTask;
import com.skyi.collector.model.CollectorTaskSchedule;
import com.skyi.collector.repository.CollectorTaskRepository;
import com.skyi.collector.repository.CollectorTaskScheduleRepository;
import com.skyi.collector.service.CollectorTaskExecutor;
import com.skyi.collector.service.CollectorTaskScheduleService;
import com.skyi.collector.service.CollectorTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 采集任务调度服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorTaskScheduleServiceImpl implements CollectorTaskScheduleService {

    private final CollectorTaskScheduleRepository scheduleRepository;
    private final CollectorTaskRepository taskRepository;
    private final CollectorTaskService taskService;
    private final CollectorTaskExecutor taskExecutor;

    @Override
    @Transactional
    public CollectorTaskScheduleDTO createSchedule(CollectorTaskScheduleDTO scheduleDTO) {
        // 验证任务是否存在
        taskRepository.findById(scheduleDTO.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + scheduleDTO.getTaskId()));

        // 计算下次执行时间
        calculateNextExecuteTime(scheduleDTO);

        // 创建调度
        CollectorTaskSchedule schedule = new CollectorTaskSchedule();
        BeanUtils.copyProperties(scheduleDTO, schedule);
        schedule.setCreateTime(LocalDateTime.now());
        schedule.setUpdateTime(LocalDateTime.now());

        // 保存调度
        schedule = scheduleRepository.save(schedule);

        // 转换为DTO并返回
        scheduleDTO.setId(schedule.getId());
        scheduleDTO.setCreateTime(schedule.getCreateTime());
        scheduleDTO.setUpdateTime(schedule.getUpdateTime());
        return scheduleDTO;
    }

    @Override
    @Transactional
    public CollectorTaskScheduleDTO updateSchedule(Long id, CollectorTaskScheduleDTO scheduleDTO) {
        // 获取现有调度
        CollectorTaskSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("调度不存在: " + id));

        // 如果任务ID有变化，验证新任务是否存在
        if (!schedule.getTaskId().equals(scheduleDTO.getTaskId())) {
            taskRepository.findById(scheduleDTO.getTaskId())
                    .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + scheduleDTO.getTaskId()));
        }

        // 计算下次执行时间
        calculateNextExecuteTime(scheduleDTO);

        // 更新调度
        BeanUtils.copyProperties(scheduleDTO, schedule);
        schedule.setId(id); // 确保ID不变
        schedule.setUpdateTime(LocalDateTime.now());

        // 保存调度
        schedule = scheduleRepository.save(schedule);

        // 转换为DTO并返回
        BeanUtils.copyProperties(schedule, scheduleDTO);
        return scheduleDTO;
    }

    @Override
    @Transactional
    public void deleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
    }

    @Override
    public CollectorTaskScheduleDTO getScheduleById(Long id) {
        CollectorTaskSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("调度不存在: " + id));

        CollectorTaskScheduleDTO scheduleDTO = new CollectorTaskScheduleDTO();
        BeanUtils.copyProperties(schedule, scheduleDTO);

        // 获取任务名称
        taskRepository.findById(schedule.getTaskId()).ifPresent(task -> {
            scheduleDTO.setTaskName(task.getName());
        });

        return scheduleDTO;
    }

    @Override
    public Page<CollectorTaskScheduleDTO> listSchedules(String name, Long taskId, Integer enabled, Pageable pageable) {
        // 构建查询条件
        Specification<CollectorTaskSchedule> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }

            if (taskId != null) {
                predicates.add(cb.equal(root.get("taskId"), taskId));
            }

            if (enabled != null) {
                predicates.add(cb.equal(root.get("enabled"), enabled));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<CollectorTaskSchedule> schedulePage = scheduleRepository.findAll(spec, pageable);

        // 转换为DTO列表
        return schedulePage.map(schedule -> {
            CollectorTaskScheduleDTO dto = new CollectorTaskScheduleDTO();
            BeanUtils.copyProperties(schedule, dto);

            // 获取任务名称
            taskRepository.findById(schedule.getTaskId()).ifPresent(task -> {
                dto.setTaskName(task.getName());
            });

            return dto;
        });
    }

    @Override
    public List<CollectorTaskScheduleDTO> listSchedulesByTaskId(Long taskId) {
        // 查询调度列表
        List<CollectorTaskSchedule> schedules = scheduleRepository.findByTaskId(taskId);

        // 获取任务名称（如果有）
        String taskName = taskRepository.findById(taskId)
                .map(task -> task.getName())
                .orElse(null);

        // 转换为DTO列表
        return schedules.stream().map(schedule -> {
            CollectorTaskScheduleDTO dto = new CollectorTaskScheduleDTO();
            BeanUtils.copyProperties(schedule, dto);
            dto.setTaskName(taskName);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CollectorTaskScheduleDTO enableSchedule(Long id) {
        CollectorTaskSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("调度不存在: " + id));

        schedule.setEnabled(1);
        schedule.setUpdateTime(LocalDateTime.now());
        schedule = scheduleRepository.save(schedule);

        CollectorTaskScheduleDTO dto = new CollectorTaskScheduleDTO();
        BeanUtils.copyProperties(schedule, dto);
        return dto;
    }

    @Override
    @Transactional
    public CollectorTaskScheduleDTO disableSchedule(Long id) {
        CollectorTaskSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("调度不存在: " + id));

        schedule.setEnabled(0);
        schedule.setUpdateTime(LocalDateTime.now());
        schedule = scheduleRepository.save(schedule);

        CollectorTaskScheduleDTO dto = new CollectorTaskScheduleDTO();
        BeanUtils.copyProperties(schedule, dto);
        return dto;
    }

    @Override
    @Transactional
    public Long executeScheduleNow(Long id) {
        CollectorTaskSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("调度不存在: " + id));

        // 获取任务对象
        Long taskId = schedule.getTaskId();
        CollectorTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));

        // 执行任务
        Long instanceId = taskExecutor.executeTask(task);

        // 更新最后执行时间
        schedule.setLastExecuteTime(LocalDateTime.now());
        scheduleRepository.save(schedule);

        return instanceId;
    }

    @Override
    public void calculateNextExecuteTime(CollectorTaskScheduleDTO scheduleDTO) {
        // 根据调度类型计算下一次执行时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextExecuteTime = null;

        switch (scheduleDTO.getScheduleType()) {
            case 1: // 固定频率
                if (scheduleDTO.getFixedRate() != null && scheduleDTO.getFixedRate() > 0) {
                    nextExecuteTime = now.plus(scheduleDTO.getFixedRate(), ChronoUnit.SECONDS);
                }
                break;
            case 2: // Cron表达式
                // 这里需要实现Cron表达式解析逻辑
                // 通常使用类似Quartz的CronExpression
                // 简化处理，默认设为1小时后
                nextExecuteTime = now.plus(1, ChronoUnit.HOURS);
                break;
            case 3: // 一次性执行
                if (scheduleDTO.getExecuteTime() != null) {
                    nextExecuteTime = scheduleDTO.getExecuteTime();
                }
                break;
            default:
                break;
        }

        // 检查是否在有效期内
        if (nextExecuteTime != null) {
            if (scheduleDTO.getStartTime() != null && nextExecuteTime.isBefore(scheduleDTO.getStartTime())) {
                nextExecuteTime = scheduleDTO.getStartTime();
            }

            if (scheduleDTO.getEndTime() != null && nextExecuteTime.isAfter(scheduleDTO.getEndTime())) {
                nextExecuteTime = null; // 超出有效期，不再执行
            }
        }

        scheduleDTO.setNextExecuteTime(nextExecuteTime);
    }
} 