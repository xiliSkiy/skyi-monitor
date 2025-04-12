package com.skyi.asset.service;

import com.skyi.asset.dto.AssetDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 资产服务接口
 */
public interface AssetService {
    
    /**
     * 创建资产
     *
     * @param assetDTO 资产信息
     * @return 创建后的资产信息
     */
    AssetDTO createAsset(AssetDTO assetDTO);
    
    /**
     * 更新资产
     *
     * @param id 资产ID
     * @param assetDTO 资产信息
     * @return 更新后的资产信息
     */
    AssetDTO updateAsset(Long id, AssetDTO assetDTO);
    
    /**
     * 删除资产
     *
     * @param id 资产ID
     */
    void deleteAsset(Long id);
    
    /**
     * 根据ID查询资产
     *
     * @param id 资产ID
     * @return 资产信息
     */
    AssetDTO getAssetById(Long id);
    
    /**
     * 根据编码查询资产
     *
     * @param code 资产编码
     * @return 资产信息
     */
    AssetDTO getAssetByCode(String code);
    
    /**
     * 分页查询资产列表
     *
     * @param name 资产名称
     * @param type 资产类型
     * @param status 状态
     * @param pageable 分页参数
     * @return 资产分页列表
     */
    Page<AssetDTO> listAssets(String name, String type, Integer status, Pageable pageable);
    
    /**
     * 根据类型查询资产列表
     *
     * @param type 资产类型
     * @return 资产列表
     */
    List<AssetDTO> listAssetsByType(String type);
    
    /**
     * 根据部门查询资产列表
     *
     * @param department 部门
     * @return 资产列表
     */
    List<AssetDTO> listAssetsByDepartment(String department);
    
    /**
     * 根据负责人查询资产列表
     *
     * @param owner 负责人
     * @return 资产列表
     */
    List<AssetDTO> listAssetsByOwner(String owner);
} 