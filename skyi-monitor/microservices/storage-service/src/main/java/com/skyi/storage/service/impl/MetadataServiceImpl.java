package com.skyi.storage.service.impl;

import com.skyi.storage.dto.MetadataDTO;
import com.skyi.storage.model.MetadataEntity;
import com.skyi.storage.repository.MetadataRepository;
import com.skyi.storage.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 元数据服务实现
 */
@Slf4j
@Service
public class MetadataServiceImpl implements MetadataService {

    @Autowired
    private MetadataRepository metadataRepository;

    @Override
    @Transactional
    public MetadataDTO saveMetadata(MetadataDTO dto) {
        try {
            MetadataEntity entity = convertToEntity(dto);
            entity = metadataRepository.save(entity);
            return convertToDTO(entity);
        } catch (Exception e) {
            log.error("保存元数据失败", e);
            throw new RuntimeException("保存元数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<MetadataDTO> saveBatchMetadata(List<MetadataDTO> dtoList) {
        try {
            List<MetadataEntity> entityList = dtoList.stream()
                    .map(this::convertToEntity)
                    .collect(Collectors.toList());
            entityList = metadataRepository.saveAll(entityList);
            return entityList.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("批量保存元数据失败", e);
            throw new RuntimeException("批量保存元数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "metadata", key = "'id:' + #id")
    public MetadataDTO getMetadataById(Long id) {
        try {
            Optional<MetadataEntity> entityOpt = metadataRepository.findById(id);
            return entityOpt.map(this::convertToDTO).orElse(null);
        } catch (Exception e) {
            log.error("查询元数据失败, id={}", id, e);
            return null;
        }
    }

    @Override
    @Cacheable(value = "metadata", key = "'type:' + #type + ':refId:' + #refId")
    public List<MetadataDTO> getMetadataByTypeAndRefId(String type, String refId) {
        try {
            List<MetadataEntity> entityList = metadataRepository.findByTypeAndRefId(type, refId);
            return entityList.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询元数据失败, type={}, refId={}", type, refId, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Cacheable(value = "metadata", key = "'type:' + #type + ':refId:' + #refId + ':key:' + #metaKey")
    public MetadataDTO getMetadataByTypeAndRefIdAndKey(String type, String refId, String metaKey) {
        try {
            Optional<MetadataEntity> entityOpt = metadataRepository.findByTypeAndRefIdAndMetaKey(type, refId, metaKey);
            return entityOpt.map(this::convertToDTO).orElse(null);
        } catch (Exception e) {
            log.error("查询元数据失败, type={}, refId={}, metaKey={}", type, refId, metaKey, e);
            return null;
        }
    }

    @Override
    public List<MetadataDTO> searchMetadataByKey(String type, String keyPattern) {
        try {
            List<MetadataEntity> entityList = metadataRepository.findByTypeAndMetaKeyLike(type, keyPattern);
            return entityList.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据键模糊查询元数据失败, type={}, keyPattern={}", type, keyPattern, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<MetadataDTO> searchMetadataByValue(String type, String valuePattern) {
        try {
            List<MetadataEntity> entityList = metadataRepository.findByTypeAndMetaValueLike(type, valuePattern);
            return entityList.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据值模糊查询元数据失败, type={}, valuePattern={}", type, valuePattern, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "metadata", key = "'id:' + #dto.id")
    public MetadataDTO updateMetadata(MetadataDTO dto) {
        try {
            if (dto.getId() == null) {
                throw new IllegalArgumentException("更新元数据时，ID不能为空");
            }
            
            Optional<MetadataEntity> entityOpt = metadataRepository.findById(dto.getId());
            if (!entityOpt.isPresent()) {
                throw new IllegalArgumentException("更新的元数据不存在, id=" + dto.getId());
            }
            
            MetadataEntity entity = entityOpt.get();
            // 只更新元数据值，其他字段不变
            entity.setMetaValue(dto.getMetaValue());
            entity = metadataRepository.save(entity);
            return convertToDTO(entity);
        } catch (Exception e) {
            log.error("更新元数据失败, dto={}", dto, e);
            throw new RuntimeException("更新元数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "metadata", key = "'id:' + #id")
    public boolean deleteMetadata(Long id) {
        try {
            if (!metadataRepository.existsById(id)) {
                return false;
            }
            metadataRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            log.error("删除元数据失败, id={}", id, e);
            return false;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "metadata", allEntries = true)
    public boolean deleteMetadataByTypeAndRefId(String type, String refId) {
        try {
            metadataRepository.deleteByTypeAndRefId(type, refId);
            return true;
        } catch (Exception e) {
            log.error("删除元数据失败, type={}, refId={}", type, refId, e);
            return false;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "metadata", allEntries = true)
    public boolean deleteMetadataByTypeAndRefIdAndKey(String type, String refId, String metaKey) {
        try {
            metadataRepository.deleteByTypeAndRefIdAndMetaKey(type, refId, metaKey);
            return true;
        } catch (Exception e) {
            log.error("删除元数据失败, type={}, refId={}, metaKey={}", type, refId, metaKey, e);
            return false;
        }
    }
    
    /**
     * 将DTO转换为实体
     */
    private MetadataEntity convertToEntity(MetadataDTO dto) {
        MetadataEntity entity = new MetadataEntity();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
    
    /**
     * 将实体转换为DTO
     */
    private MetadataDTO convertToDTO(MetadataEntity entity) {
        MetadataDTO dto = new MetadataDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
} 