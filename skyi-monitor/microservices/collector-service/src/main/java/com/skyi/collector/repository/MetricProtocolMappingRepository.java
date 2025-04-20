package com.skyi.collector.repository;

import com.skyi.collector.model.MetricProtocolMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * 指标协议映射数据访问接口
 */
public interface MetricProtocolMappingRepository extends JpaRepository<MetricProtocolMapping, Long>, JpaSpecificationExecutor<MetricProtocolMapping> {
    
    /**
     * 根据指标ID查询协议映射列表
     *
     * @param metricId 指标ID
     * @return 协议映射列表
     */
    List<MetricProtocolMapping> findByMetricId(Long metricId);
    
    /**
     * 根据协议查询协议映射列表
     *
     * @param protocol 协议
     * @return 协议映射列表
     */
    List<MetricProtocolMapping> findByProtocol(String protocol);
    
    /**
     * 根据指标ID和协议查询协议映射
     *
     * @param metricId 指标ID
     * @param protocol 协议
     * @return 协议映射
     */
    Optional<MetricProtocolMapping> findByMetricIdAndProtocol(Long metricId, String protocol);
    
    /**
     * 根据指标ID列表和协议查询协议映射列表
     *
     * @param metricIds 指标ID列表
     * @param protocol 协议
     * @return 协议映射列表
     */
    List<MetricProtocolMapping> findByMetricIdInAndProtocol(List<Long> metricIds, String protocol);
    
    /**
     * 删除指标的所有协议映射
     *
     * @param metricId 指标ID
     */
    void deleteByMetricId(Long metricId);
} 