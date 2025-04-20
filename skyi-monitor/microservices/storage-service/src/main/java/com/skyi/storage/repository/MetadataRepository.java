package com.skyi.storage.repository;

import com.skyi.storage.model.MetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 元数据Repository
 */
@Repository
public interface MetadataRepository extends JpaRepository<MetadataEntity, Long> {

    /**
     * 根据类型和关联ID查询元数据列表
     */
    List<MetadataEntity> findByTypeAndRefId(String type, String refId);

    /**
     * 根据类型、关联ID和元数据键查询元数据
     */
    Optional<MetadataEntity> findByTypeAndRefIdAndMetaKey(String type, String refId, String metaKey);

    /**
     * 根据类型和元数据键模糊查询
     */
    @Query("SELECT m FROM MetadataEntity m WHERE m.type = :type AND m.metaKey LIKE %:keyPattern%")
    List<MetadataEntity> findByTypeAndMetaKeyLike(@Param("type") String type, @Param("keyPattern") String keyPattern);

    /**
     * 根据类型和元数据值模糊查询
     */
    @Query("SELECT m FROM MetadataEntity m WHERE m.type = :type AND m.metaValue LIKE %:valuePattern%")
    List<MetadataEntity> findByTypeAndMetaValueLike(@Param("type") String type, @Param("valuePattern") String valuePattern);

    /**
     * 删除指定类型和关联ID的所有元数据
     */
    void deleteByTypeAndRefId(String type, String refId);

    /**
     * 删除指定类型、关联ID和元数据键的元数据
     */
    void deleteByTypeAndRefIdAndMetaKey(String type, String refId, String metaKey);
} 