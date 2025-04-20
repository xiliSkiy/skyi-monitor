package com.skyi.collector.service.impl;

import com.skyi.collector.dto.CollectorTaskInstanceDTO;
import com.skyi.collector.model.CollectorMetricData;
import com.skyi.collector.model.CollectorTask;
import com.skyi.collector.model.CollectorTaskInstance;
import com.skyi.collector.repository.CollectorMetricDataRepository;
import com.skyi.collector.repository.CollectorTaskInstanceRepository;
import com.skyi.collector.repository.CollectorTaskRepository;
import com.skyi.collector.service.CollectorTaskInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 采集任务实例服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorTaskInstanceServiceImpl implements CollectorTaskInstanceService {

    private final CollectorTaskInstanceRepository instanceRepository;
    private final CollectorTaskRepository taskRepository;
    private final CollectorMetricDataRepository metricDataRepository;

    @Override
    public CollectorTaskInstanceDTO getInstanceById(Long id) {
        CollectorTaskInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("任务实例不存在: " + id));
        
        return convertToDTO(instance);
    }

    @Override
    public Page<CollectorTaskInstanceDTO> listInstances(Long taskId, Integer status, 
                                            String startTimeStr, String endTimeStr, Pageable pageable) {
        // 解析时间字符串
        final LocalDateTime[] startTimeHolder = { null };
        final LocalDateTime[] endTimeHolder = { null };
        
        if (startTimeStr != null && !startTimeStr.isEmpty()) {
            try {
                startTimeHolder[0] = LocalDateTime.parse(startTimeStr, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e) {
                log.warn("解析开始时间失败: {}", startTimeStr);
            }
        }
        
        if (endTimeStr != null && !endTimeStr.isEmpty()) {
            try {
                endTimeHolder[0] = LocalDateTime.parse(endTimeStr, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e) {
                log.warn("解析结束时间失败: {}", endTimeStr);
            }
        }
        
        // 构建查询条件
        Specification<CollectorTaskInstance> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (taskId != null) {
                predicates.add(cb.equal(root.get("taskId"), taskId));
            }
            
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            if (startTimeHolder[0] != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), startTimeHolder[0]));
            }
            
            if (endTimeHolder[0] != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startTime"), endTimeHolder[0]));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        // 执行查询
        Page<CollectorTaskInstance> instancePage = instanceRepository.findAll(spec, pageable);
        
        // 转换为DTO列表
        return instancePage.map(this::convertToDTO);
    }

    @Override
    public CollectorTaskInstanceDTO getLatestInstance(Long taskId) {
        Optional<CollectorTaskInstance> instanceOpt = 
                instanceRepository.findTopByTaskIdOrderByStartTimeDesc(taskId);
        
        if (instanceOpt.isPresent()) {
            return convertToDTO(instanceOpt.get());
        } else {
            return null;
        }
    }
    
    /**
     * 将实体转换为DTO
     *
     * @param instance 任务实例实体
     * @return 任务实例DTO
     */
    private CollectorTaskInstanceDTO convertToDTO(CollectorTaskInstance instance) {
        CollectorTaskInstanceDTO dto = new CollectorTaskInstanceDTO();
        BeanUtils.copyProperties(instance, dto);
        
        // 设置任务名称、类型等附加信息
        Optional<CollectorTask> taskOpt = taskRepository.findById(instance.getTaskId());
        taskOpt.ifPresent(task -> {
            dto.setTaskName(task.getName());
            dto.setTaskType(task.getType());
        });
        
        // 计算执行时长
        if (instance.getStartTime() != null && instance.getEndTime() != null) {
            long duration = java.time.Duration.between(
                    instance.getStartTime(), instance.getEndTime()).toMillis();
            dto.setDuration(duration);
        }
        
        // 获取采集指标数量
        long metricCount = metricDataRepository.countByInstanceId(instance.getId());
        dto.setMetricCount((int) metricCount);
        
        return dto;
    }
} 