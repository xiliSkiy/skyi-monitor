package com.skyi.collector.controller;

import com.skyi.collector.dto.MetricDefinitionDTO;
import com.skyi.collector.service.MetricDefinitionService;
import com.skyi.common.model.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 指标定义控制器
 */
@Slf4j
@RestController
@RequestMapping("/collector/metrics")
@RequiredArgsConstructor
public class MetricDefinitionController {
    
    private final MetricDefinitionService metricService;
    
    /**
     * 创建指标定义
     *
     * @param metricDTO 指标信息
     * @return 创建结果
     */
    @PostMapping
    public Result<MetricDefinitionDTO> createMetric(@RequestBody @Validated MetricDefinitionDTO metricDTO) {
        log.info("创建指标定义请求：{}", metricDTO.getName());
        MetricDefinitionDTO created = metricService.createMetric(metricDTO);
        return Result.success(created);
    }
    
    /**
     * 更新指标定义
     *
     * @param id 指标ID
     * @param metricDTO 指标信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<MetricDefinitionDTO> updateMetric(@PathVariable Long id, 
                                            @RequestBody @Validated MetricDefinitionDTO metricDTO) {
        log.info("更新指标定义请求：id={}", id);
        MetricDefinitionDTO updated = metricService.updateMetric(id, metricDTO);
        return Result.success(updated);
    }
    
    /**
     * 删除指标定义
     *
     * @param id 指标ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteMetric(@PathVariable Long id) {
        log.info("删除指标定义请求：id={}", id);
        metricService.deleteMetric(id);
        return Result.success();
    }
    
    /**
     * 查询指标定义
     *
     * @param id 指标ID
     * @return 指标信息
     */
    @GetMapping("/{id}")
    public Result<MetricDefinitionDTO> getMetricById(@PathVariable Long id) {
        log.info("查询指标定义请求：id={}", id);
        MetricDefinitionDTO metric = metricService.getMetricById(id);
        return Result.success(metric);
    }
    
    /**
     * 根据编码查询指标定义
     *
     * @param code 指标编码
     * @return 指标信息
     */
    @GetMapping("/code/{code}")
    public Result<MetricDefinitionDTO> getMetricByCode(@PathVariable String code) {
        log.info("根据编码查询指标定义请求：code={}", code);
        MetricDefinitionDTO metric = metricService.getMetricByCode(code);
        return Result.success(metric);
    }
    
    /**
     * 分页查询指标列表
     *
     * @param code 指标编码
     * @param name 指标名称
     * @param category 指标类别
     * @param collectionMethod 采集方式
     * @param status 状态
     * @param page 页码
     * @param size 每页大小
     * @return 指标分页列表
     */
    @GetMapping
    public Result<Page<MetricDefinitionDTO>> listMetrics(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String collectionMethod,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("分页查询指标定义请求：code={}, name={}, category={}, collectionMethod={}, status={}, page={}, size={}", 
                code, name, category, collectionMethod, status, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        Page<MetricDefinitionDTO> metricPage = metricService.listMetrics(code, name, category, collectionMethod, status, pageable);
        return Result.success(metricPage);
    }
    
    /**
     * 查询资产类型对应的指标列表
     *
     * @param assetType 资产类型
     * @return 指标列表
     */
    @GetMapping("/asset-type/{assetType}")
    public Result<List<MetricDefinitionDTO>> listMetricsByAssetType(@PathVariable String assetType) {
        log.info("查询资产类型对应的指标列表请求：assetType={}", assetType);
        List<MetricDefinitionDTO> metrics = metricService.listMetricsByAssetType(assetType);
        return Result.success(metrics);
    }
    
    /**
     * 启用指标
     *
     * @param id 指标ID
     * @return 更新结果
     */
    @PutMapping("/{id}/enable")
    public Result<MetricDefinitionDTO> enableMetric(@PathVariable Long id) {
        log.info("启用指标定义请求：id={}", id);
        MetricDefinitionDTO updated = metricService.enableMetric(id);
        return Result.success(updated);
    }
    
    /**
     * 禁用指标
     *
     * @param id 指标ID
     * @return 更新结果
     */
    @PutMapping("/{id}/disable")
    public Result<MetricDefinitionDTO> disableMetric(@PathVariable Long id) {
        log.info("禁用指标定义请求：id={}", id);
        MetricDefinitionDTO updated = metricService.disableMetric(id);
        return Result.success(updated);
    }
    
    /**
     * 导入指标定义
     *
     * @param metrics 指标列表
     * @return 导入结果
     */
    @PostMapping("/import")
    public Result<List<MetricDefinitionDTO>> importMetrics(@RequestBody List<MetricDefinitionDTO> metrics) {
        log.info("导入指标定义请求：size={}", metrics.size());
        List<MetricDefinitionDTO> imported = metricService.importMetrics(metrics);
        return Result.success(imported);
    }
    
    /**
     * 导出指标定义
     *
     * @param category 指标类别
     * @return 指标列表
     */
    @GetMapping("/export")
    public Result<List<MetricDefinitionDTO>> exportMetrics(@RequestParam(required = false) String category) {
        log.info("导出指标定义请求：category={}", category);
        List<MetricDefinitionDTO> metrics = metricService.exportMetrics(category);
        return Result.success(metrics);
    }
}