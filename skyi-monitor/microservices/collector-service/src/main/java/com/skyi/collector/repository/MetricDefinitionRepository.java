package com.skyi.collector.repository;

import com.skyi.collector.model.MetricDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * 指标定义数据访问接口
 */
public interface MetricDefinitionRepository extends JpaRepository<MetricDefinition, Long>, JpaSpecificationExecutor<MetricDefinition> {
    
    /**
     * 根据指标编码查询指标
     *
     * @param code 指标编码
     * @return 指标信息
     */
    Optional<MetricDefinition> findByCode(String code);
    
    /**
     * 根据指标类别查询指标列表
     *
     * @param category 指标类别
     * @return 指标列表
     */
    List<MetricDefinition> findByCategory(String category);
    
    /**
     * 根据采集方式查询指标列表
     *
     * @param collectionMethod 采集方式
     * @return 指标列表
     */
    List<MetricDefinition> findByCollectionMethod(String collectionMethod);
    
    /**
     * 根据状态查询指标列表
     *
     * @param status 状态
     * @return 指标列表
     */
    List<MetricDefinition> findByStatus(Integer status);
    
    /**
     * 根据编码前缀查询指标列表
     *
     * @param codePrefix 编码前缀
     * @return 指标列表
     */
    @Query("SELECT m FROM MetricDefinition m WHERE m.code LIKE CONCAT(:codePrefix, '%')")
    List<MetricDefinition> findByCodeStartingWith(String codePrefix);
    
    /**
     * 根据名称模糊查询指标列表
     *
     * @param name 名称关键字
     * @return 指标列表
     */
    List<MetricDefinition> findByNameContaining(String name);
} 