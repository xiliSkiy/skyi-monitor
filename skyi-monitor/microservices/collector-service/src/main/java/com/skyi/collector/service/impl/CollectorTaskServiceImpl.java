package com.skyi.collector.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyi.collector.dto.CollectorTaskDTO;
import com.skyi.collector.model.CollectorTask;
import com.skyi.collector.repository.CollectorTaskRepository;
import com.skyi.collector.service.CollectorTaskExecutor;
import com.skyi.collector.service.CollectorTaskService;
import com.skyi.collector.service.collector.Collector;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 采集任务服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorTaskServiceImpl implements CollectorTaskService {

    private final CollectorTaskRepository taskRepository;
    private final CollectorTaskExecutor taskExecutor;
    private final List<Collector> collectors;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public CollectorTaskDTO createTask(CollectorTaskDTO taskDTO) {
        // 验证采集器是否支持
        validateCollector(taskDTO.getType(), taskDTO.getProtocol());
        
        // 验证指标配置
        validateMetrics(taskDTO);
        
        // 创建采集任务
        CollectorTask task = new CollectorTask();
        BeanUtils.copyProperties(taskDTO, task);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        
        // 设置初始状态
        if (task.getStatus() == null) {
            task.setStatus(0); // 默认禁用
        }
        
        // 保存任务
        task = taskRepository.save(task);
        
        // 转换为DTO并返回
        taskDTO.setId(task.getId());
        taskDTO.setCreateTime(task.getCreateTime());
        taskDTO.setUpdateTime(task.getUpdateTime());
        return taskDTO;
    }

    @Override
    @Transactional
    public CollectorTaskDTO updateTask(Long id, CollectorTaskDTO taskDTO) {
        // 验证采集器是否支持
        validateCollector(taskDTO.getType(), taskDTO.getProtocol());
        
        // 验证指标配置
        validateMetrics(taskDTO);
        
        // 获取现有任务
        CollectorTask task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + id));
        
        // 更新任务
        BeanUtils.copyProperties(taskDTO, task);
        task.setId(id); // 确保ID不变
        task.setUpdateTime(LocalDateTime.now());
        
        // 保存任务
        task = taskRepository.save(task);
        
        // 转换为DTO并返回
        BeanUtils.copyProperties(task, taskDTO);
        return taskDTO;
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    public CollectorTaskDTO getTaskById(Long id) {
        CollectorTask task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + id));
        
        CollectorTaskDTO taskDTO = new CollectorTaskDTO();
        BeanUtils.copyProperties(task, taskDTO);
        return taskDTO;
    }

    @Override
    public CollectorTaskDTO getTaskByCode(String code) {
        CollectorTask task = taskRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + code));
        
        CollectorTaskDTO taskDTO = new CollectorTaskDTO();
        BeanUtils.copyProperties(task, taskDTO);
        return taskDTO;
    }

    @Override
    public Page<CollectorTaskDTO> listTasks(String name, String type, Integer status, Pageable pageable) {
        // 构建查询条件
        Specification<CollectorTask> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            
            if (type != null && !type.isEmpty()) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        // 执行查询
        Page<CollectorTask> taskPage = taskRepository.findAll(spec, pageable);
        
        // 转换为DTO列表
        return taskPage.map(task -> {
            CollectorTaskDTO dto = new CollectorTaskDTO();
            BeanUtils.copyProperties(task, dto);
            return dto;
        });
    }

    @Override
    public List<CollectorTaskDTO> listTasksByType(String type) {
        List<CollectorTask> tasks = taskRepository.findByType(type);
        
        return tasks.stream().map(task -> {
            CollectorTaskDTO dto = new CollectorTaskDTO();
            BeanUtils.copyProperties(task, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<CollectorTaskDTO> listTasksByAssetId(Long assetId) {
        List<CollectorTask> tasks = taskRepository.findByAssetId(assetId);
        
        return tasks.stream().map(task -> {
            CollectorTaskDTO dto = new CollectorTaskDTO();
            BeanUtils.copyProperties(task, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<CollectorTaskDTO> listTasksByProtocol(String protocol) {
        List<CollectorTask> tasks = taskRepository.findByProtocol(protocol);
        
        return tasks.stream().map(task -> {
            CollectorTaskDTO dto = new CollectorTaskDTO();
            BeanUtils.copyProperties(task, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CollectorTaskDTO startTask(Long id) {
        CollectorTask task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + id));
        
        task.setStatus(1); // 1表示启用
        task.setUpdateTime(LocalDateTime.now());
        task = taskRepository.save(task);
        
        CollectorTaskDTO taskDTO = new CollectorTaskDTO();
        BeanUtils.copyProperties(task, taskDTO);
        return taskDTO;
    }

    @Override
    @Transactional
    public CollectorTaskDTO stopTask(Long id) {
        CollectorTask task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + id));
        
        task.setStatus(0); // 0表示禁用
        task.setUpdateTime(LocalDateTime.now());
        task = taskRepository.save(task);
        
        CollectorTaskDTO taskDTO = new CollectorTaskDTO();
        BeanUtils.copyProperties(task, taskDTO);
        return taskDTO;
    }

    @Override
    @Transactional
    public Long executeTaskNow(Long id) {
        CollectorTask task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + id));
        
        // 立即执行任务
        return taskExecutor.executeTask(task);
    }
    
    /**
     * 验证采集器是否支持指定的类型和协议
     */
    private void validateCollector(String type, String protocol) {
        Optional<Collector> collector = collectors.stream()
                .filter(c -> c.getType().equals(type) && c.getSupportedProtocols().contains(protocol))
                .findFirst();
        
        if (collector.isEmpty()) {
            throw new IllegalArgumentException("不支持的采集器类型或协议: " + type + "/" + protocol);
        }
    }
    
    /**
     * 验证指标配置
     */
    private void validateMetrics(CollectorTaskDTO taskDTO) {
        // 获取对应的采集器
        Optional<Collector> collector = collectors.stream()
                .filter(c -> c.getType().equals(taskDTO.getType()) && c.getSupportedProtocols().contains(taskDTO.getProtocol()))
                .findFirst();
        
        if (collector.isPresent()) {
            try {
                // 将指标列表转换为JSON
                String metricsJson = convertMetricsToJson(taskDTO.getMetrics());
                
                // 调用采集器的验证方法
                String errorMsg = collector.get().validateMetrics(metricsJson);
                if (errorMsg != null) {
                    throw new IllegalArgumentException("指标配置错误: " + errorMsg);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("指标配置格式错误: " + e.getMessage());
            }
        }
    }
    
    /**
     * 将指标列表转换为JSON字符串
     */
    private String convertMetricsToJson(List<CollectorTaskDTO.MetricDTO> metrics) {
        try {
            return objectMapper.writeValueAsString(metrics);
        } catch (JsonProcessingException e) {
            log.error("转换指标列表为JSON失败", e);
            throw new IllegalArgumentException("指标格式错误: " + e.getMessage());
        }
    }
} 