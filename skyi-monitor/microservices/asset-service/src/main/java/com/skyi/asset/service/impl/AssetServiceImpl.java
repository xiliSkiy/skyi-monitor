package com.skyi.asset.service.impl;

import com.skyi.asset.dto.AssetDTO;
import com.skyi.asset.model.Asset;
import com.skyi.asset.model.AssetTag;
import com.skyi.asset.repository.AssetRepository;
import com.skyi.asset.repository.AssetTagRepository;
import com.skyi.asset.service.AssetService;
import com.skyi.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 资产服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {
    
    private final AssetRepository assetRepository;
    private final AssetTagRepository assetTagRepository;
    
    @Override
    @Transactional
    public AssetDTO createAsset(AssetDTO assetDTO) {
        // 检查资产编码是否已存在
        if (assetRepository.findByCode(assetDTO.getCode()).isPresent()) {
            throw new BusinessException("资产编码已存在");
        }
        
        // 转换DTO为实体
        Asset asset = convertToEntity(assetDTO);
        
        // 保存资产
        Asset savedAsset = assetRepository.save(asset);
        log.info("创建资产成功：{}", savedAsset.getCode());
        
        // 保存标签
        saveAssetTags(savedAsset.getId(), assetDTO.getTags());
        
        // 返回创建后的资产信息
        return getAssetById(savedAsset.getId());
    }
    
    @Override
    @Transactional
    public AssetDTO updateAsset(Long id, AssetDTO assetDTO) {
        // 检查资产是否存在
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new BusinessException("资产不存在"));
        
        // 如果编码变更，检查新编码是否已存在
        if (!asset.getCode().equals(assetDTO.getCode()) && 
                assetRepository.findByCode(assetDTO.getCode()).isPresent()) {
            throw new BusinessException("资产编码已存在");
        }
        
        // 更新资产信息
        asset.setName(assetDTO.getName());
        asset.setCode(assetDTO.getCode());
        asset.setType(assetDTO.getType());
        asset.setIpAddress(assetDTO.getIpAddress());
        asset.setPort(assetDTO.getPort());
        asset.setDepartment(assetDTO.getDepartment());
        asset.setOwner(assetDTO.getOwner());
        asset.setStatus(assetDTO.getStatus());
        asset.setDescription(assetDTO.getDescription());
        
        // 保存更新
        Asset updatedAsset = assetRepository.save(asset);
        log.info("更新资产成功：{}", updatedAsset.getCode());
        
        // 更新标签
        assetTagRepository.deleteByAssetId(id);
        saveAssetTags(id, assetDTO.getTags());
        
        // 返回更新后的资产信息
        return getAssetById(id);
    }
    
    @Override
    @Transactional
    public void deleteAsset(Long id) {
        // 检查资产是否存在
        if (!assetRepository.existsById(id)) {
            throw new BusinessException("资产不存在");
        }
        
        // 删除资产标签
        assetTagRepository.deleteByAssetId(id);
        
        // 删除资产
        assetRepository.deleteById(id);
        log.info("删除资产成功：id={}", id);
    }
    
    @Override
    public AssetDTO getAssetById(Long id) {
        // 查询资产
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new BusinessException("资产不存在"));
        
        // 查询资产标签
        List<AssetTag> tags = assetTagRepository.findByAssetId(id);
        
        // 转换为DTO并返回
        return convertToDTO(asset, tags);
    }
    
    @Override
    public AssetDTO getAssetByCode(String code) {
        // 查询资产
        Asset asset = assetRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException("资产不存在"));
        
        // 查询资产标签
        List<AssetTag> tags = assetTagRepository.findByAssetId(asset.getId());
        
        // 转换为DTO并返回
        return convertToDTO(asset, tags);
    }
    
    @Override
    public Page<AssetDTO> listAssets(String name, String type, Integer status, Pageable pageable) {
        // 构建查询条件
        Asset probe = new Asset();
        if (StringUtils.hasText(type)) {
            probe.setType(type);
        }
        if (status != null) {
            probe.setStatus(status);
        }
        
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        
        if (StringUtils.hasText(name)) {
            matcher = matcher.withMatcher("name", match -> match.contains());
        }
        
        // 分页查询
        Page<Asset> assetPage = assetRepository.findAll(Example.of(probe, matcher), pageable);
        
        // 转换为DTO
        return assetPage.map(asset -> {
            List<AssetTag> tags = assetTagRepository.findByAssetId(asset.getId());
            return convertToDTO(asset, tags);
        });
    }
    
    @Override
    public List<AssetDTO> listAssetsByType(String type) {
        List<Asset> assets = assetRepository.findByType(type);
        return assets.stream()
                .map(asset -> {
                    List<AssetTag> tags = assetTagRepository.findByAssetId(asset.getId());
                    return convertToDTO(asset, tags);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<AssetDTO> listAssetsByDepartment(String department) {
        List<Asset> assets = assetRepository.findByDepartment(department);
        return assets.stream()
                .map(asset -> {
                    List<AssetTag> tags = assetTagRepository.findByAssetId(asset.getId());
                    return convertToDTO(asset, tags);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<AssetDTO> listAssetsByOwner(String owner) {
        List<Asset> assets = assetRepository.findByOwner(owner);
        return assets.stream()
                .map(asset -> {
                    List<AssetTag> tags = assetTagRepository.findByAssetId(asset.getId());
                    return convertToDTO(asset, tags);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 保存资产标签
     *
     * @param assetId 资产ID
     * @param tagDTOs 标签DTO列表
     */
    private void saveAssetTags(Long assetId, List<AssetDTO.TagDTO> tagDTOs) {
        if (tagDTOs == null || tagDTOs.isEmpty()) {
            return;
        }
        
        List<AssetTag> tags = tagDTOs.stream()
                .map(tagDTO -> {
                    AssetTag tag = new AssetTag();
                    tag.setAssetId(assetId);
                    tag.setTagKey(tagDTO.getTagKey());
                    tag.setTagValue(tagDTO.getTagValue());
                    return tag;
                })
                .collect(Collectors.toList());
        
        assetTagRepository.saveAll(tags);
    }
    
    /**
     * 将DTO转换为实体
     *
     * @param assetDTO 资产DTO
     * @return 资产实体
     */
    private Asset convertToEntity(AssetDTO assetDTO) {
        Asset asset = new Asset();
        asset.setName(assetDTO.getName());
        asset.setCode(assetDTO.getCode());
        asset.setType(assetDTO.getType());
        asset.setIpAddress(assetDTO.getIpAddress());
        asset.setPort(assetDTO.getPort());
        asset.setDepartment(assetDTO.getDepartment());
        asset.setOwner(assetDTO.getOwner());
        asset.setStatus(assetDTO.getStatus());
        asset.setDescription(assetDTO.getDescription());
        return asset;
    }
    
    /**
     * 将实体转换为DTO
     *
     * @param asset 资产实体
     * @param tags 标签列表
     * @return 资产DTO
     */
    private AssetDTO convertToDTO(Asset asset, List<AssetTag> tags) {
        AssetDTO assetDTO = new AssetDTO();
        assetDTO.setId(asset.getId());
        assetDTO.setName(asset.getName());
        assetDTO.setCode(asset.getCode());
        assetDTO.setType(asset.getType());
        assetDTO.setIpAddress(asset.getIpAddress());
        assetDTO.setPort(asset.getPort());
        assetDTO.setDepartment(asset.getDepartment());
        assetDTO.setOwner(asset.getOwner());
        assetDTO.setStatus(asset.getStatus());
        assetDTO.setDescription(asset.getDescription());
        assetDTO.setCreateTime(asset.getCreateTime());
        assetDTO.setUpdateTime(asset.getUpdateTime());
        
        // 转换标签
        if (tags != null && !tags.isEmpty()) {
            List<AssetDTO.TagDTO> tagDTOs = tags.stream()
                    .map(tag -> {
                        AssetDTO.TagDTO tagDTO = new AssetDTO.TagDTO();
                        tagDTO.setId(tag.getId());
                        tagDTO.setTagKey(tag.getTagKey());
                        tagDTO.setTagValue(tag.getTagValue());
                        return tagDTO;
                    })
                    .collect(Collectors.toList());
            assetDTO.setTags(tagDTOs);
        } else {
            assetDTO.setTags(new ArrayList<>());
        }
        
        return assetDTO;
    }
} 