package com.skyi.collector.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyi.collector.dto.MetricAssetTypeMappingDTO;
import com.skyi.collector.dto.MetricDefinitionDTO;
import com.skyi.collector.dto.MetricProtocolMappingDTO;
import com.skyi.collector.model.MetricAssetTypeMapping;
import com.skyi.collector.model.MetricDefinition;
import com.skyi.collector.model.MetricProtocolMapping;
import com.skyi.collector.repository.MetricAssetTypeMappingRepository;
import com.skyi.collector.repository.MetricDefinitionRepository;
import com.skyi.collector.repository.MetricProtocolMappingRepository;
import com.skyi.collector.service.MetricDefinitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 指标定义服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricDefinitionServiceImpl implements MetricDefinitionService {

    private final MetricDefinitionRepository metricRepository;
    private final MetricProtocolMappingRepository protocolMappingRepository;
    private final MetricAssetTypeMappingRepository assetTypeMappingRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public MetricDefinitionDTO createMetric(MetricDefinitionDTO metricDTO) {
        // 检查编码是否存在
        if (metricRepository.findByCode(metricDTO.getCode()).isPresent()) {
            throw new IllegalArgumentException("指标编码已存在: " + metricDTO.getCode());
        }
        
        // 创建指标定义
        MetricDefinition metric = new MetricDefinition();
        BeanUtils.copyProperties(metricDTO, metric);
        metric.setCreatedTime(LocalDateTime.now());
        metric.setUpdatedTime(LocalDateTime.now());
        
        // 保存指标定义
        metric = metricRepository.save(metric);
        Long metricId = metric.getId();
        
        // 保存协议映射
        saveProtocolMappings(metricId, metricDTO.getProtocolMappings());
        
        // 保存资产类型映射
        saveAssetTypeMappings(metricId, metricDTO.getAssetTypeMappings());
        
        // 返回完整的DTO
        return getMetricById(metricId);
    }

    @Override
    @Transactional
    public MetricDefinitionDTO updateMetric(Long id, MetricDefinitionDTO metricDTO) {
        // 获取现有指标
        MetricDefinition metric = metricRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("指标不存在: " + id));
        
        // 检查编码是否与其他指标冲突
        Optional<MetricDefinition> existingMetric = metricRepository.findByCode(metricDTO.getCode());
        if (existingMetric.isPresent() && !existingMetric.get().getId().equals(id)) {
            throw new IllegalArgumentException("指标编码已被其他指标使用: " + metricDTO.getCode());
        }
        
        // 更新指标基本信息
        BeanUtils.copyProperties(metricDTO, metric);
        metric.setId(id); // 确保ID不变
        metric.setUpdatedTime(LocalDateTime.now());
        
        // 保存指标定义
        metric = metricRepository.save(metric);
        
        // 更新协议映射
        protocolMappingRepository.deleteByMetricId(id);
        saveProtocolMappings(id, metricDTO.getProtocolMappings());
        
        // 更新资产类型映射
        assetTypeMappingRepository.deleteByMetricId(id);
        saveAssetTypeMappings(id, metricDTO.getAssetTypeMappings());
        
        // 返回完整的DTO
        return getMetricById(id);
    }

    @Override
    @Transactional
    public void deleteMetric(Long id) {
        // 检查指标是否存在
        if (!metricRepository.existsById(id)) {
            throw new IllegalArgumentException("指标不存在: " + id);
        }
        
        // 删除关联的协议映射
        protocolMappingRepository.deleteByMetricId(id);
        
        // 删除关联的资产类型映射
        assetTypeMappingRepository.deleteByMetricId(id);
        
        // 删除指标定义
        metricRepository.deleteById(id);
    }

    @Override
    public MetricDefinitionDTO getMetricById(Long id) {
        MetricDefinition metric = metricRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("指标不存在: " + id));
        
        return convertToDTO(metric);
    }

    @Override
    public MetricDefinitionDTO getMetricByCode(String code) {
        MetricDefinition metric = metricRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("指标不存在: " + code));
        
        return convertToDTO(metric);
    }

    @Override
    public Page<MetricDefinitionDTO> listMetrics(String code, String name, String category, 
                                               String collectionMethod, Integer status, Pageable pageable) {
        // 构建查询条件
        Specification<MetricDefinition> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(code)) {
                predicates.add(cb.like(root.get("code"), "%" + code + "%"));
            }
            
            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            
            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            
            if (StringUtils.hasText(collectionMethod)) {
                predicates.add(cb.equal(root.get("collectionMethod"), collectionMethod));
            }
            
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        // 执行查询
        Page<MetricDefinition> metricPage = metricRepository.findAll(spec, pageable);
        
        // 转换为DTO
        return metricPage.map(this::convertToDTO);
    }

    @Override
    public List<MetricDefinitionDTO> listMetricsByAssetType(String assetType) {
        // 查询指定资产类型关联的有效指标ID列表
        List<Long> metricIds = assetTypeMappingRepository.findActiveMetricIdsByAssetType(assetType);
        
        if (metricIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 根据ID列表查询指标定义
        List<MetricDefinition> metrics = metricRepository.findAllById(metricIds);
        
        // 转换为DTO
        return metrics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MetricDefinitionDTO enableMetric(Long id) {
        MetricDefinition metric = metricRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("指标不存在: " + id));
        
        metric.setStatus(1);
        metric.setUpdatedTime(LocalDateTime.now());
        
        metric = metricRepository.save(metric);
        
        return convertToDTO(metric);
    }

    @Override
    @Transactional
    public MetricDefinitionDTO disableMetric(Long id) {
        MetricDefinition metric = metricRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("指标不存在: " + id));
        
        metric.setStatus(0);
        metric.setUpdatedTime(LocalDateTime.now());
        
        metric = metricRepository.save(metric);
        
        return convertToDTO(metric);
    }

    @Override
    @Transactional
    public List<MetricDefinitionDTO> importMetrics(List<MetricDefinitionDTO> metrics) {
        List<MetricDefinitionDTO> result = new ArrayList<>();
        
        for (MetricDefinitionDTO metricDTO : metrics) {
            try {
                // 检查指标是否已存在
                Optional<MetricDefinition> existingMetric = metricRepository.findByCode(metricDTO.getCode());
                
                if (existingMetric.isPresent()) {
                    // 更新现有指标
                    result.add(updateMetric(existingMetric.get().getId(), metricDTO));
                } else {
                    // 创建新指标
                    result.add(createMetric(metricDTO));
                }
            } catch (Exception e) {
                log.error("导入指标失败: {}", metricDTO.getCode(), e);
            }
        }
        
        return result;
    }

    @Override
    public List<MetricDefinitionDTO> exportMetrics(String category) {
        List<MetricDefinition> metrics;
        
        if (StringUtils.hasText(category)) {
            metrics = metricRepository.findByCategory(category);
        } else {
            metrics = metricRepository.findAll();
        }
        
        return metrics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将实体转换为DTO
     *
     * @param metric 实体对象
     * @return DTO对象
     */
    private MetricDefinitionDTO convertToDTO(MetricDefinition metric) {
        MetricDefinitionDTO dto = new MetricDefinitionDTO();
        BeanUtils.copyProperties(metric, dto);
        
        // 查询并添加协议映射
        List<MetricProtocolMapping> protocolMappings = protocolMappingRepository.findByMetricId(metric.getId());
        dto.setProtocolMappings(protocolMappings.stream()
                .map(this::convertToProtocolMappingDTO)
                .collect(Collectors.toList()));
        
        // 查询并添加资产类型映射
        List<MetricAssetTypeMapping> assetTypeMappings = assetTypeMappingRepository.findByMetricId(metric.getId());
        dto.setAssetTypeMappings(assetTypeMappings.stream()
                .map(this::convertToAssetTypeMappingDTO)
                .collect(Collectors.toList()));
        
        return dto;
    }
    
    /**
     * 将协议映射实体转换为DTO
     *
     * @param mapping 协议映射实体
     * @return 协议映射DTO
     */
    private MetricProtocolMappingDTO convertToProtocolMappingDTO(MetricProtocolMapping mapping) {
        MetricProtocolMappingDTO dto = new MetricProtocolMappingDTO();
        BeanUtils.copyProperties(mapping, dto);
        return dto;
    }
    
    /**
     * 将资产类型映射实体转换为DTO
     *
     * @param mapping 资产类型映射实体
     * @return 资产类型映射DTO
     */
    private MetricAssetTypeMappingDTO convertToAssetTypeMappingDTO(MetricAssetTypeMapping mapping) {
        MetricAssetTypeMappingDTO dto = new MetricAssetTypeMappingDTO();
        BeanUtils.copyProperties(mapping, dto);
        return dto;
    }
    
    /**
     * 保存协议映射列表
     *
     * @param metricId 指标ID
     * @param mappings 协议映射DTO列表
     */
    private void saveProtocolMappings(Long metricId, List<MetricProtocolMappingDTO> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            return;
        }
        
        for (MetricProtocolMappingDTO mappingDTO : mappings) {
            MetricProtocolMapping mapping = new MetricProtocolMapping();
            BeanUtils.copyProperties(mappingDTO, mapping);
            mapping.setMetricId(metricId);
            mapping.setCreatedTime(LocalDateTime.now());
            mapping.setUpdatedTime(LocalDateTime.now());
            
            protocolMappingRepository.save(mapping);
        }
    }
    
    /**
     * 保存资产类型映射列表
     *
     * @param metricId 指标ID
     * @param mappings 资产类型映射DTO列表
     */
    private void saveAssetTypeMappings(Long metricId, List<MetricAssetTypeMappingDTO> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            return;
        }
        
        for (MetricAssetTypeMappingDTO mappingDTO : mappings) {
            MetricAssetTypeMapping mapping = new MetricAssetTypeMapping();
            BeanUtils.copyProperties(mappingDTO, mapping);
            mapping.setMetricId(metricId);
            mapping.setCreatedTime(LocalDateTime.now());
            mapping.setUpdatedTime(LocalDateTime.now());
            
            assetTypeMappingRepository.save(mapping);
        }
    }
} 