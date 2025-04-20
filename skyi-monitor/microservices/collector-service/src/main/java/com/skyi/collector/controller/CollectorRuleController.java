package com.skyi.collector.controller;

import com.skyi.collector.dto.CollectorRuleDTO;
import com.skyi.collector.service.CollectorRuleService;
import com.skyi.common.model.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 调度规则控制器
 */
@Slf4j
@RestController
@RequestMapping("/collector/rules")
@RequiredArgsConstructor
public class CollectorRuleController {
    
    private final CollectorRuleService ruleService;
    
    /**
     * 创建调度规则
     *
     * @param ruleDTO 规则信息
     * @return 创建结果
     */
    @PostMapping
    public Result<CollectorRuleDTO> createRule(@RequestBody @Validated CollectorRuleDTO ruleDTO) {
        log.info("创建调度规则请求：{}", ruleDTO.getName());
        CollectorRuleDTO created = ruleService.createRule(ruleDTO);
        return Result.success(created);
    }
    
    /**
     * 更新调度规则
     *
     * @param id 规则ID
     * @param ruleDTO 规则信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<CollectorRuleDTO> updateRule(@PathVariable Long id, 
                                           @RequestBody @Validated CollectorRuleDTO ruleDTO) {
        log.info("更新调度规则请求：id={}", id);
        CollectorRuleDTO updated = ruleService.updateRule(id, ruleDTO);
        return Result.success(updated);
    }
    
    /**
     * 删除调度规则
     *
     * @param id 规则ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRule(@PathVariable Long id) {
        log.info("删除调度规则请求：id={}", id);
        ruleService.deleteRule(id);
        return Result.success();
    }
    
    /**
     * 查询调度规则
     *
     * @param id 规则ID
     * @return 规则信息
     */
    @GetMapping("/{id}")
    public Result<CollectorRuleDTO> getRuleById(@PathVariable Long id) {
        log.info("查询调度规则请求：id={}", id);
        CollectorRuleDTO rule = ruleService.getRuleById(id);
        return Result.success(rule);
    }
    
    /**
     * 分页查询规则列表
     *
     * @param name 规则名称
     * @param type 规则类型
     * @param status 状态
     * @param page 页码
     * @param size 每页大小
     * @return 规则分页列表
     */
    @GetMapping
    public Result<Page<CollectorRuleDTO>> listRules(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("分页查询调度规则请求：name={}, type={}, status={}, page={}, size={}", 
                name, type, status, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<CollectorRuleDTO> rulePage = ruleService.listRules(name, type, status, pageable);
        return Result.success(rulePage);
    }
    
    /**
     * 启用规则
     *
     * @param id 规则ID
     * @return 更新结果
     */
    @PutMapping("/{id}/enable")
    public Result<CollectorRuleDTO> enableRule(@PathVariable Long id) {
        log.info("启用调度规则请求：id={}", id);
        CollectorRuleDTO updated = ruleService.enableRule(id);
        return Result.success(updated);
    }
    
    /**
     * 禁用规则
     *
     * @param id 规则ID
     * @return 更新结果
     */
    @PutMapping("/{id}/disable")
    public Result<CollectorRuleDTO> disableRule(@PathVariable Long id) {
        log.info("禁用调度规则请求：id={}", id);
        CollectorRuleDTO updated = ruleService.disableRule(id);
        return Result.success(updated);
    }
} 