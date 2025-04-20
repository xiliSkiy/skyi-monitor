package com.skyi.collector.service;

import com.skyi.collector.dto.CollectorRuleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 调度规则服务接口
 */
public interface CollectorRuleService {

    /**
     * 创建调度规则
     *
     * @param ruleDTO 规则信息
     * @return 创建的规则
     */
    CollectorRuleDTO createRule(CollectorRuleDTO ruleDTO);

    /**
     * 更新调度规则
     *
     * @param id 规则ID
     * @param ruleDTO 规则信息
     * @return 更新后的规则
     */
    CollectorRuleDTO updateRule(Long id, CollectorRuleDTO ruleDTO);

    /**
     * 删除调度规则
     *
     * @param id 规则ID
     */
    void deleteRule(Long id);

    /**
     * 根据ID查询规则
     *
     * @param id 规则ID
     * @return 规则信息
     */
    CollectorRuleDTO getRuleById(Long id);

    /**
     * 根据名称查询规则
     *
     * @param name 规则名称
     * @return 规则信息
     */
    CollectorRuleDTO getRuleByName(String name);

    /**
     * 分页查询规则列表
     *
     * @param name 规则名称
     * @param type 规则类型
     * @param status 状态
     * @param pageable 分页参数
     * @return 规则分页列表
     */
    Page<CollectorRuleDTO> listRules(String name, String type, Integer status, Pageable pageable);

    /**
     * 启用规则
     *
     * @param id 规则ID
     * @return 更新后的规则
     */
    CollectorRuleDTO enableRule(Long id);

    /**
     * 禁用规则
     *
     * @param id 规则ID
     * @return 更新后的规则
     */
    CollectorRuleDTO disableRule(Long id);

    /**
     * 计算规则的下次执行时间
     *
     * @param id 规则ID
     */
    void calculateNextExecuteTime(Long id);

    /**
     * 查询需要执行的规则列表
     *
     * @return 需要执行的规则列表
     */
    List<CollectorRuleDTO> findRulesNeedExecute();
} 