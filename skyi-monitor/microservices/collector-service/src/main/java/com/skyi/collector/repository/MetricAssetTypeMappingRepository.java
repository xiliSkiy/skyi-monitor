package com.skyi.collector.repository;

import com.skyi.collector.model.MetricAssetTypeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * 指标资产类型映射数据访问接口
 */
public interface MetricAssetTypeMappingRepository extends JpaRepository<MetricAssetTypeMapping, Long>, JpaSpecificationExecutor<MetricAssetTypeMapping> {
    
    /**
     * 根据指标ID查询资产类型映射列表
     *
     * @param metricId 指标ID
     * @return 资产类型映射列表
     */
    List<MetricAssetTypeMapping> findByMetricId(Long metricId);
    
    /**
     * 根据资产类型查询映射列表
     *
     * @param assetType 资产类型
     * @return 映射列表
     */
    List<MetricAssetTypeMapping> findByAssetType(String assetType);
    
    /**
     * 根据资产类型和默认启用状态查询映射列表
     *
     * @param assetType 资产类型
     * @param defaultEnabled 默认启用状态
     * @return 映射列表
     */
    List<MetricAssetTypeMapping> findByAssetTypeAndDefaultEnabled(String assetType, Integer defaultEnabled);
    
    /**
     * 根据指标ID和资产类型查询映射
     *
     * @param metricId 指标ID
     * @param assetType 资产类型
     * @return 映射信息
     */
    Optional<MetricAssetTypeMapping> findByMetricIdAndAssetType(Long metricId, String assetType);
    
    /**
     * 删除指标的所有资产类型映射
     *
     * @param metricId 指标ID
     */
    void deleteByMetricId(Long metricId);
    
    /**
     * 根据资产类型查询有效指标ID列表
     *
     * @param assetType 资产类型
     * @return 指标ID列表
     */
    @Query("SELECT m.metricId FROM MetricAssetTypeMapping m WHERE m.assetType = :assetType AND m.defaultEnabled = 1")
    List<Long> findActiveMetricIdsByAssetType(String assetType);
} 