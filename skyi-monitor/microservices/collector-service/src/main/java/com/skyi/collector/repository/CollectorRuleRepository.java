package com.skyi.collector.repository;

import com.skyi.collector.model.CollectorRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 调度规则数据访问接口
 */
public interface CollectorRuleRepository extends JpaRepository<CollectorRule, Long>, JpaSpecificationExecutor<CollectorRule> {
    
    /**
     * 根据规则名称查询规则
     *
     * @param name 规则名称
     * @return 规则信息
     */
    Optional<CollectorRule> findByName(String name);
    
    /**
     * 根据类型查询规则列表
     *
     * @param type 规则类型
     * @return 规则列表
     */
    List<CollectorRule> findByType(String type);
    
    /**
     * 根据状态查询规则列表
     *
     * @param status 状态
     * @return 规则列表
     */
    List<CollectorRule> findByStatus(Integer status);
    
    /**
     * 查询需要执行的规则列表
     *
     * @param currentTime 当前时间
     * @return 需要执行的规则列表
     */
    @Query("SELECT r FROM CollectorRule r WHERE r.status = 1 AND (r.nextExecuteTime IS NULL OR r.nextExecuteTime <= :currentTime)")
    List<CollectorRule> findRulesNeedExecute(LocalDateTime currentTime);
} 