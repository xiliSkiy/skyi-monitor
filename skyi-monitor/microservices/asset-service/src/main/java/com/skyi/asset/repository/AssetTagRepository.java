package com.skyi.asset.repository;

import com.skyi.asset.model.AssetTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 资产标签数据访问接口
 */
public interface AssetTagRepository extends JpaRepository<AssetTag, Long> {
    
    /**
     * 根据资产ID查询标签列表
     *
     * @param assetId 资产ID
     * @return 标签列表
     */
    List<AssetTag> findByAssetId(Long assetId);
    
    /**
     * 根据资产ID删除标签
     *
     * @param assetId 资产ID
     */
    void deleteByAssetId(Long assetId);
    
    /**
     * 根据标签键和标签值查询标签列表
     *
     * @param tagKey 标签键
     * @param tagValue 标签值
     * @return 标签列表
     */
    List<AssetTag> findByTagKeyAndTagValue(String tagKey, String tagValue);
} 