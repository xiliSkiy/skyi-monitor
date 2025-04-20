package com.skyi.collector.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyi.collector.dto.CollectorRuleDTO;
import com.skyi.collector.model.CollectorRule;
import com.skyi.collector.repository.CollectorRuleRepository;
import com.skyi.collector.service.CollectorRuleService;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 调度规则服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorRuleServiceImpl implements CollectorRuleService {

    private final CollectorRuleRepository ruleRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public CollectorRuleDTO createRule(CollectorRuleDTO ruleDTO) {
        // 创建规则实体
        CollectorRule rule = new CollectorRule();
        BeanUtils.copyProperties(ruleDTO, rule);
        
        // 手动处理taskIds字段，将List<Long>转换为JSON字符串
        try {
            rule.setTaskIds(objectMapper.writeValueAsString(ruleDTO.getTaskIds()));
        } catch (JsonProcessingException e) {
            log.error("转换任务ID列表为JSON失败", e);
            throw new IllegalArgumentException("任务ID列表格式错误: " + e.getMessage());
        }
        
        // 计算下次执行时间
        calculateNextExecuteTimeForRule(rule);
        
        // 设置创建时间和更新时间
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        
        // 保存规则
        rule = ruleRepository.save(rule);
        
        // 转换为DTO并返回
        ruleDTO.setId(rule.getId());
        ruleDTO.setCreateTime(rule.getCreateTime());
        ruleDTO.setUpdateTime(rule.getUpdateTime());
        return ruleDTO;
    }

    @Override
    @Transactional
    public CollectorRuleDTO updateRule(Long id, CollectorRuleDTO ruleDTO) {
        // 获取现有规则
        CollectorRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("规则不存在: " + id));
        
        // 更新规则
        BeanUtils.copyProperties(ruleDTO, rule);
        rule.setId(id); // 确保ID不变
        
        // 手动处理taskIds字段，将List<Long>转换为JSON字符串
        try {
            rule.setTaskIds(objectMapper.writeValueAsString(ruleDTO.getTaskIds()));
        } catch (JsonProcessingException e) {
            log.error("转换任务ID列表为JSON失败", e);
            throw new IllegalArgumentException("任务ID列表格式错误: " + e.getMessage());
        }
        
        // 计算下次执行时间
        calculateNextExecuteTimeForRule(rule);
        
        rule.setUpdateTime(LocalDateTime.now());
        
        // 保存规则
        rule = ruleRepository.save(rule);
        
        // 转换为DTO并返回
        ruleDTO.setId(rule.getId());
        ruleDTO.setCreateTime(rule.getCreateTime());
        ruleDTO.setUpdateTime(rule.getUpdateTime());
        return ruleDTO;
    }

    @Override
    @Transactional
    public void deleteRule(Long id) {
        ruleRepository.deleteById(id);
    }

    @Override
    public CollectorRuleDTO getRuleById(Long id) {
        CollectorRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("规则不存在: " + id));
        
        return convertToDTO(rule);
    }

    @Override
    public CollectorRuleDTO getRuleByName(String name) {
        CollectorRule rule = ruleRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("规则不存在: " + name));
        
        return convertToDTO(rule);
    }

    @Override
    public Page<CollectorRuleDTO> listRules(String name, String type, Integer status, Pageable pageable) {
        // 构建查询条件
        Specification<CollectorRule> spec = (root, query, cb) -> {
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
        Page<CollectorRule> rulePage = ruleRepository.findAll(spec, pageable);
        
        // 转换为DTO列表
        return rulePage.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public CollectorRuleDTO enableRule(Long id) {
        CollectorRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("规则不存在: " + id));
        
        rule.setStatus(1); // 启用
        calculateNextExecuteTimeForRule(rule); // 重新计算下次执行时间
        rule.setUpdateTime(LocalDateTime.now());
        
        rule = ruleRepository.save(rule);
        
        return convertToDTO(rule);
    }

    @Override
    @Transactional
    public CollectorRuleDTO disableRule(Long id) {
        CollectorRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("规则不存在: " + id));
        
        rule.setStatus(0); // 禁用
        rule.setNextExecuteTime(null); // 清除下次执行时间
        rule.setUpdateTime(LocalDateTime.now());
        
        rule = ruleRepository.save(rule);
        
        return convertToDTO(rule);
    }

    @Override
    @Transactional
    public void calculateNextExecuteTime(Long id) {
        CollectorRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("规则不存在: " + id));
        
        calculateNextExecuteTimeForRule(rule);
        
        ruleRepository.save(rule);
    }

    @Override
    public List<CollectorRuleDTO> findRulesNeedExecute() {
        List<CollectorRule> rules = ruleRepository.findRulesNeedExecute(LocalDateTime.now());
        
        return rules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将实体转换为DTO
     *
     * @param rule 规则实体
     * @return 规则DTO
     */
    private CollectorRuleDTO convertToDTO(CollectorRule rule) {
        CollectorRuleDTO dto = new CollectorRuleDTO();
        BeanUtils.copyProperties(rule, dto);
        
        // 手动处理taskIds字段，将JSON字符串转换为List<Long>
        if (rule.getTaskIds() != null && !rule.getTaskIds().isEmpty()) {
            try {
                List<Long> taskIds = objectMapper.readValue(rule.getTaskIds(), new TypeReference<List<Long>>() {});
                dto.setTaskIds(taskIds);
            } catch (Exception e) {
                log.error("解析任务ID列表JSON失败", e);
            }
        }
        
        return dto;
    }
    
    /**
     * 计算规则的下次执行时间
     *
     * @param rule 规则实体
     */
    private void calculateNextExecuteTimeForRule(CollectorRule rule) {
        // 只有启用状态的规则才计算下次执行时间
        if (rule.getStatus() != 1) {
            rule.setNextExecuteTime(null);
            return;
        }
        
        LocalDateTime nextExecuteTime = null;
        LocalDateTime now = LocalDateTime.now();
        
        try {
            switch (rule.getType()) {
                case "cron":
                    // 这里简化处理，实际应使用Quartz等库解析cron表达式
                    // 为了演示，这里简单设置为1小时后执行
                    nextExecuteTime = now.plusHours(1);
                    break;
                    
                case "interval":
                    // 间隔执行，下次执行时间 = 当前时间 + 间隔秒数
                    int intervalSeconds = Integer.parseInt(rule.getExpression());
                    nextExecuteTime = now.plusSeconds(intervalSeconds);
                    break;
                    
                case "fixedTime":
                    // 固定时间执行，解析时间字符串（格式为HH:mm:ss）
                    String timeStr = rule.getExpression();
                    LocalDateTime today = now.toLocalDate().atTime(
                            LocalDateTime.parse(now.toLocalDate() + "T" + timeStr)
                                    .toLocalTime());
                    
                    // 如果今天的执行时间已过，则安排到明天执行
                    if (today.isBefore(now)) {
                        nextExecuteTime = today.plusDays(1);
                    } else {
                        nextExecuteTime = today;
                    }
                    break;
                    
                default:
                    log.warn("不支持的规则类型: {}", rule.getType());
                    break;
            }
        } catch (Exception e) {
            log.error("计算下次执行时间失败: {}", e.getMessage(), e);
        }
        
        rule.setNextExecuteTime(nextExecuteTime);
    }
} 