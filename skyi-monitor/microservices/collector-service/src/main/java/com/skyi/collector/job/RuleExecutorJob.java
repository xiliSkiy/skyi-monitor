package com.skyi.collector.job;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyi.collector.dto.CollectorRuleDTO;
import com.skyi.collector.model.CollectorRule;
import com.skyi.collector.model.CollectorTask;
import com.skyi.collector.repository.CollectorRuleRepository;
import com.skyi.collector.repository.CollectorTaskRepository;
import com.skyi.collector.service.CollectorRuleService;
import com.skyi.collector.service.CollectorTaskExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 调度规则执行器任务
 * 负责检查和执行调度规则
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RuleExecutorJob {

    private final CollectorRuleService ruleService;
    private final CollectorTaskRepository taskRepository;
    private final CollectorRuleRepository ruleRepository;
    private final CollectorTaskExecutor taskExecutor;
    private final ObjectMapper objectMapper;

    /**
     * 执行调度规则
     * 每15秒检查一次
     */
    @Scheduled(fixedRate = 15000)
    public void executeRules() {
        LocalDateTime now = LocalDateTime.now();
        log.debug("检查调度规则: {}", now);

        try {
            // 查询需要执行的规则
            List<CollectorRuleDTO> rules = ruleService.findRulesNeedExecute();
            log.info("发现{}个调度规则需要执行", rules.size());

            for (CollectorRuleDTO ruleDTO : rules) {
                try {
                    executeRule(ruleDTO);
                } catch (Exception e) {
                    log.error("执行调度规则出错: ruleId={}, {}", ruleDTO.getId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("检查调度规则过程出错: {}", e.getMessage(), e);
        }
    }

    /**
     * 执行单个调度规则
     *
     * @param ruleDTO 规则DTO
     */
    @Transactional
    public void executeRule(CollectorRuleDTO ruleDTO) {
        log.info("执行调度规则: {}", ruleDTO.getName());
        
        // 获取关联的任务ID列表
        List<Long> taskIds = ruleDTO.getTaskIds();
        if (taskIds == null || taskIds.isEmpty()) {
            log.warn("规则没有关联任务: {}", ruleDTO.getName());
            return;
        }

        // 查询并执行所有关联的任务
        List<Long> executedTaskIds = new ArrayList<>();
        for (Long taskId : taskIds) {
            try {
                CollectorTask task = taskRepository.findById(taskId).orElse(null);
                if (task == null) {
                    log.warn("任务不存在: taskId={}", taskId);
                    continue;
                }

                // 执行任务
                log.info("规则[{}]触发执行任务: {}", ruleDTO.getName(), task.getName());
                taskExecutor.scheduleTask(task, false);
                executedTaskIds.add(taskId);
            } catch (Exception e) {
                log.error("规则执行任务出错: ruleId={}, taskId={}, {}", 
                          ruleDTO.getId(), taskId, e.getMessage(), e);
            }
        }

        // 更新规则的执行时间
        if (!executedTaskIds.isEmpty()) {
            updateRuleExecutionTime(ruleDTO.getId());
        }
    }

    /**
     * 更新规则的执行时间并计算下次执行时间
     *
     * @param ruleId 规则ID
     */
    @Transactional
    public void updateRuleExecutionTime(Long ruleId) {
        try {
            // 获取规则实体
            CollectorRule rule = ruleRepository.findById(ruleId).orElse(null);
            if (rule == null) {
                return;
            }

            // 更新上次执行时间
            rule.setLastExecuteTime(LocalDateTime.now());
            
            // 计算下次执行时间
            ruleService.calculateNextExecuteTime(ruleId);
            
            log.debug("已更新规则执行时间: ruleId={}", ruleId);
        } catch (Exception e) {
            log.error("更新规则执行时间出错: ruleId={}, {}", ruleId, e.getMessage(), e);
        }
    }
} 