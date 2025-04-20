package com.skyi.collector.service;

import com.skyi.collector.dto.MetricDefinitionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 指标定义服务接口
 */
public interface MetricDefinitionService {
    
    /**
     * 创建指标定义
     *
     * @param metricDTO 指标信息
     * @return 创建的指标
     */
    MetricDefinitionDTO createMetric(MetricDefinitionDTO metricDTO);
    
    /**
     * 更新指标定义
     *
     * @param id 指标ID
     * @param metricDTO 指标信息
     * @return 更新后的指标
     */
    MetricDefinitionDTO updateMetric(Long id, MetricDefinitionDTO metricDTO);
    
    /**
     * 删除指标定义
     *
     * @param id 指标ID
     */
    void deleteMetric(Long id);
    
    /**
     * 根据ID查询指标
     *
     * @param id 指标ID
     * @return 指标信息
     */
    MetricDefinitionDTO getMetricById(Long id);
    
    /**
     * 根据编码查询指标
     *
     * @param code 指标编码
     * @return 指标信息
     */
    MetricDefinitionDTO getMetricByCode(String code);
    
    /**
     * 分页查询指标列表
     *
     * @param code 指标编码
     * @param name 指标名称
     * @param category 指标类别
     * @param collectionMethod 采集方式
     * @param status 状态
     * @param pageable 分页参数
     * @return 指标分页列表
     */
    Page<MetricDefinitionDTO> listMetrics(String code, String name, String category, 
                                         String collectionMethod, Integer status, Pageable pageable);
    
    /**
     * 查询资产类型对应的指标列表
     *
     * @param assetType 资产类型
     * @return 指标列表
     */
    List<MetricDefinitionDTO> listMetricsByAssetType(String assetType);
    
    /**
     * 启用指标
     *
     * @param id 指标ID
     * @return 更新后的指标
     */
    MetricDefinitionDTO enableMetric(Long id);
    
    /**
     * 禁用指标
     *
     * @param id 指标ID
     * @return 更新后的指标
     */
    MetricDefinitionDTO disableMetric(Long id);
    
    /**
     * 导入指标定义
     *
     * @param metrics 指标列表
     * @return 导入结果
     */
    List<MetricDefinitionDTO> importMetrics(List<MetricDefinitionDTO> metrics);
    
    /**
     * 导出指标定义
     *
     * @param category 指标类别
     * @return 指标列表
     */
    List<MetricDefinitionDTO> exportMetrics(String category);
} 