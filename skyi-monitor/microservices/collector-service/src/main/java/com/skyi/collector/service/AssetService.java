package com.skyi.collector.service;

import java.util.List;
import java.util.Map;

/**
 * 资产服务接口
 * 负责获取和操作资产信息
 */
public interface AssetService {
    
    /**
     * 根据资产ID获取资产基本信息
     * 
     * @param assetId 资产ID
     * @return 资产基本信息，包含name、ip、type等字段
     */
    Map<String, Object> getAssetBasicInfo(Long assetId);
    
    /**
     * 根据资产ID获取资产详细信息
     * 
     * @param assetId 资产ID
     * @return 资产详细信息
     */
    Map<String, Object> getAssetDetailInfo(Long assetId);
    
    /**
     * 根据资产类型获取资产列表
     * 
     * @param assetType 资产类型
     * @return 资产列表
     */
    List<Map<String, Object>> getAssetsByType(String assetType);
    
    /**
     * 检查资产是否存在
     * 
     * @param assetId 资产ID
     * @return 是否存在
     */
    boolean existsAsset(Long assetId);
    
    /**
     * 获取资产的连接参数
     * 
     * @param assetId 资产ID
     * @param protocol 协议
     * @return 连接参数
     */
    Map<String, Object> getAssetConnectionParams(Long assetId, String protocol);
} 