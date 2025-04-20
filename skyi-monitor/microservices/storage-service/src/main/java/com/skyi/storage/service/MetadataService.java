package com.skyi.storage.service;

import com.skyi.storage.dto.MetadataDTO;

import java.util.List;

/**
 * 元数据服务接口
 */
public interface MetadataService {

    /**
     * 保存元数据
     *
     * @param dto 元数据DTO
     * @return 保存后的元数据DTO
     */
    MetadataDTO saveMetadata(MetadataDTO dto);

    /**
     * 批量保存元数据
     *
     * @param dtoList 元数据DTO列表
     * @return 保存后的元数据DTO列表
     */
    List<MetadataDTO> saveBatchMetadata(List<MetadataDTO> dtoList);

    /**
     * 根据ID查询元数据
     *
     * @param id 元数据ID
     * @return 元数据DTO
     */
    MetadataDTO getMetadataById(Long id);

    /**
     * 根据类型和关联ID查询元数据列表
     *
     * @param type  元数据类型
     * @param refId 关联ID
     * @return 元数据DTO列表
     */
    List<MetadataDTO> getMetadataByTypeAndRefId(String type, String refId);

    /**
     * 根据类型、关联ID和元数据键查询元数据
     *
     * @param type    元数据类型
     * @param refId   关联ID
     * @param metaKey 元数据键
     * @return 元数据DTO
     */
    MetadataDTO getMetadataByTypeAndRefIdAndKey(String type, String refId, String metaKey);

    /**
     * 根据元数据键模糊查询
     *
     * @param type       元数据类型
     * @param keyPattern 元数据键模式
     * @return 元数据DTO列表
     */
    List<MetadataDTO> searchMetadataByKey(String type, String keyPattern);

    /**
     * 根据元数据值模糊查询
     *
     * @param type         元数据类型
     * @param valuePattern 元数据值模式
     * @return 元数据DTO列表
     */
    List<MetadataDTO> searchMetadataByValue(String type, String valuePattern);

    /**
     * 更新元数据
     *
     * @param dto 元数据DTO
     * @return 更新后的元数据DTO
     */
    MetadataDTO updateMetadata(MetadataDTO dto);

    /**
     * 删除元数据
     *
     * @param id 元数据ID
     * @return 是否删除成功
     */
    boolean deleteMetadata(Long id);

    /**
     * 删除指定类型和关联ID的所有元数据
     *
     * @param type  元数据类型
     * @param refId 关联ID
     * @return 是否删除成功
     */
    boolean deleteMetadataByTypeAndRefId(String type, String refId);

    /**
     * 删除指定类型、关联ID和元数据键的元数据
     *
     * @param type    元数据类型
     * @param refId   关联ID
     * @param metaKey 元数据键
     * @return 是否删除成功
     */
    boolean deleteMetadataByTypeAndRefIdAndKey(String type, String refId, String metaKey);
} 