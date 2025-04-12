package com.skyi.asset.service;

import com.skyi.asset.dto.AssetDTO;
import com.skyi.asset.model.Asset;
import com.skyi.asset.model.AssetTag;
import com.skyi.asset.repository.AssetRepository;
import com.skyi.asset.repository.AssetTagRepository;
import com.skyi.asset.service.impl.AssetServiceImpl;
import com.skyi.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 资产服务实现类单元测试
 */
@ExtendWith(MockitoExtension.class)
public class AssetServiceImplTest {
    
    @Mock
    private AssetRepository assetRepository;
    
    @Mock
    private AssetTagRepository assetTagRepository;
    
    @InjectMocks
    private AssetServiceImpl assetService;
    
    /**
     * 测试创建资产
     */
    @Test
    public void testCreateAsset() {
        // 准备测试数据
        AssetDTO assetDTO = new AssetDTO();
        assetDTO.setName("测试服务器");
        assetDTO.setCode("SRV001");
        assetDTO.setType("server");
        assetDTO.setIpAddress("192.168.1.100");
        assetDTO.setPort(22);
        assetDTO.setDepartment("IT部门");
        assetDTO.setOwner("张三");
        assetDTO.setStatus(1);
        assetDTO.setDescription("测试描述");
        
        AssetDTO.TagDTO tagDTO = new AssetDTO.TagDTO();
        tagDTO.setTagKey("env");
        tagDTO.setTagValue("test");
        assetDTO.setTags(Arrays.asList(tagDTO));
        
        // 模拟仓库行为
        when(assetRepository.findByCode(assetDTO.getCode())).thenReturn(Optional.empty());
        
        Asset savedAsset = new Asset();
        savedAsset.setId(1L);
        savedAsset.setName(assetDTO.getName());
        savedAsset.setCode(assetDTO.getCode());
        savedAsset.setType(assetDTO.getType());
        savedAsset.setIpAddress(assetDTO.getIpAddress());
        savedAsset.setPort(assetDTO.getPort());
        savedAsset.setDepartment(assetDTO.getDepartment());
        savedAsset.setOwner(assetDTO.getOwner());
        savedAsset.setStatus(assetDTO.getStatus());
        savedAsset.setDescription(assetDTO.getDescription());
        savedAsset.setCreateTime(LocalDateTime.now());
        savedAsset.setUpdateTime(LocalDateTime.now());
        
        when(assetRepository.save(any(Asset.class))).thenReturn(savedAsset);
        
        AssetTag savedTag = new AssetTag();
        savedTag.setId(1L);
        savedTag.setAssetId(savedAsset.getId());
        savedTag.setTagKey(tagDTO.getTagKey());
        savedTag.setTagValue(tagDTO.getTagValue());
        savedTag.setCreateTime(LocalDateTime.now());
        savedTag.setUpdateTime(LocalDateTime.now());
        
        when(assetTagRepository.findByAssetId(savedAsset.getId())).thenReturn(Arrays.asList(savedTag));
        
        // 执行测试
        AssetDTO result = assetService.createAsset(assetDTO);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(savedAsset.getId(), result.getId());
        assertEquals(assetDTO.getName(), result.getName());
        assertEquals(assetDTO.getCode(), result.getCode());
        assertEquals(assetDTO.getType(), result.getType());
        assertEquals(assetDTO.getIpAddress(), result.getIpAddress());
        assertEquals(assetDTO.getPort(), result.getPort());
        assertEquals(assetDTO.getDepartment(), result.getDepartment());
        assertEquals(assetDTO.getOwner(), result.getOwner());
        assertEquals(assetDTO.getStatus(), result.getStatus());
        assertEquals(assetDTO.getDescription(), result.getDescription());
        assertNotNull(result.getTags());
        assertEquals(1, result.getTags().size());
        assertEquals(tagDTO.getTagKey(), result.getTags().get(0).getTagKey());
        assertEquals(tagDTO.getTagValue(), result.getTags().get(0).getTagValue());
        
        // 验证调用
        verify(assetRepository, times(1)).findByCode(assetDTO.getCode());
        verify(assetRepository, times(1)).save(any(Asset.class));
        verify(assetTagRepository, times(1)).saveAll(anyList());
        verify(assetTagRepository, times(1)).findByAssetId(savedAsset.getId());
    }
    
    /**
     * 测试创建资产 - 编码已存在
     */
    @Test
    public void testCreateAsset_CodeExists() {
        // 准备测试数据
        AssetDTO assetDTO = new AssetDTO();
        assetDTO.setName("测试服务器");
        assetDTO.setCode("SRV001");
        assetDTO.setType("server");
        
        // 模拟仓库行为
        when(assetRepository.findByCode(assetDTO.getCode())).thenReturn(Optional.of(new Asset()));
        
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            assetService.createAsset(assetDTO);
        });
        
        assertEquals("资产编码已存在", exception.getMessage());
        
        // 验证调用
        verify(assetRepository, times(1)).findByCode(assetDTO.getCode());
        verify(assetRepository, never()).save(any(Asset.class));
        verify(assetTagRepository, never()).saveAll(anyList());
    }
    
    /**
     * 测试根据ID查询资产
     */
    @Test
    public void testGetAssetById() {
        // 准备测试数据
        Long id = 1L;
        
        Asset asset = new Asset();
        asset.setId(id);
        asset.setName("测试服务器");
        asset.setCode("SRV001");
        asset.setType("server");
        asset.setIpAddress("192.168.1.100");
        asset.setPort(22);
        asset.setDepartment("IT部门");
        asset.setOwner("张三");
        asset.setStatus(1);
        asset.setDescription("测试描述");
        asset.setCreateTime(LocalDateTime.now());
        asset.setUpdateTime(LocalDateTime.now());
        
        AssetTag tag = new AssetTag();
        tag.setId(1L);
        tag.setAssetId(id);
        tag.setTagKey("env");
        tag.setTagValue("test");
        tag.setCreateTime(LocalDateTime.now());
        tag.setUpdateTime(LocalDateTime.now());
        
        // 模拟仓库行为
        when(assetRepository.findById(id)).thenReturn(Optional.of(asset));
        when(assetTagRepository.findByAssetId(id)).thenReturn(Arrays.asList(tag));
        
        // 执行测试
        AssetDTO result = assetService.getAssetById(id);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(asset.getId(), result.getId());
        assertEquals(asset.getName(), result.getName());
        assertEquals(asset.getCode(), result.getCode());
        assertEquals(asset.getType(), result.getType());
        assertEquals(asset.getIpAddress(), result.getIpAddress());
        assertEquals(asset.getPort(), result.getPort());
        assertEquals(asset.getDepartment(), result.getDepartment());
        assertEquals(asset.getOwner(), result.getOwner());
        assertEquals(asset.getStatus(), result.getStatus());
        assertEquals(asset.getDescription(), result.getDescription());
        assertNotNull(result.getTags());
        assertEquals(1, result.getTags().size());
        assertEquals(tag.getTagKey(), result.getTags().get(0).getTagKey());
        assertEquals(tag.getTagValue(), result.getTags().get(0).getTagValue());
        
        // 验证调用
        verify(assetRepository, times(1)).findById(id);
        verify(assetTagRepository, times(1)).findByAssetId(id);
    }
    
    /**
     * 测试根据ID查询资产 - 资产不存在
     */
    @Test
    public void testGetAssetById_NotFound() {
        // 准备测试数据
        Long id = 1L;
        
        // 模拟仓库行为
        when(assetRepository.findById(id)).thenReturn(Optional.empty());
        
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            assetService.getAssetById(id);
        });
        
        assertEquals("资产不存在", exception.getMessage());
        
        // 验证调用
        verify(assetRepository, times(1)).findById(id);
        verify(assetTagRepository, never()).findByAssetId(anyLong());
    }
    
    /**
     * 测试分页查询资产列表
     */
    @Test
    public void testListAssets() {
        // 准备测试数据
        String name = "服务器";
        String type = "server";
        Integer status = 1;
        Pageable pageable = PageRequest.of(0, 10);
        
        Asset asset1 = new Asset();
        asset1.setId(1L);
        asset1.setName("测试服务器1");
        asset1.setCode("SRV001");
        asset1.setType(type);
        asset1.setStatus(status);
        
        Asset asset2 = new Asset();
        asset2.setId(2L);
        asset2.setName("测试服务器2");
        asset2.setCode("SRV002");
        asset2.setType(type);
        asset2.setStatus(status);
        
        List<Asset> assets = Arrays.asList(asset1, asset2);
        Page<Asset> assetPage = new PageImpl<>(assets, pageable, assets.size());
        
        // 模拟仓库行为
        when(assetRepository.findAll(any(), eq(pageable))).thenReturn(assetPage);
        when(assetTagRepository.findByAssetId(asset1.getId())).thenReturn(new ArrayList<>());
        when(assetTagRepository.findByAssetId(asset2.getId())).thenReturn(new ArrayList<>());
        
        // 执行测试
        Page<AssetDTO> result = assetService.listAssets(name, type, status, pageable);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getContent().size());
        
        // 验证调用
        verify(assetRepository, times(1)).findAll(any(), eq(pageable));
        verify(assetTagRepository, times(1)).findByAssetId(asset1.getId());
        verify(assetTagRepository, times(1)).findByAssetId(asset2.getId());
    }
} 