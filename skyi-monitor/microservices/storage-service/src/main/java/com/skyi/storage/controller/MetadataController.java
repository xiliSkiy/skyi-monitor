package com.skyi.storage.controller;

import com.skyi.storage.dto.MetadataDTO;
import com.skyi.storage.service.MetadataService;
import com.skyi.storage.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 元数据控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/metadata")
@Tag(name = "元数据接口", description = "提供元数据的增删改查功能")
public class MetadataController {

    @Autowired
    private MetadataService metadataService;

    @PostMapping
    @Operation(summary = "保存元数据", description = "保存单条元数据")
    public Result<MetadataDTO> saveMetadata(@Valid @RequestBody MetadataDTO metadataDTO) {
        log.debug("保存元数据: {}", metadataDTO);
        MetadataDTO result = metadataService.saveMetadata(metadataDTO);
        return Result.success(result);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量保存元数据", description = "批量保存多条元数据")
    public Result<List<MetadataDTO>> saveBatchMetadata(@Valid @RequestBody List<MetadataDTO> metadataDTOList) {
        log.debug("批量保存元数据, 数量: {}", metadataDTOList.size());
        List<MetadataDTO> result = metadataService.saveBatchMetadata(metadataDTOList);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询元数据", description = "根据ID查询单条元数据")
    public Result<MetadataDTO> getMetadataById(
            @Parameter(description = "元数据ID") @PathVariable Long id) {
        log.debug("根据ID查询元数据, id: {}", id);
        MetadataDTO result = metadataService.getMetadataById(id);
        return Result.success(result);
    }

    @GetMapping("/type/{type}/ref/{refId}")
    @Operation(summary = "根据类型和关联ID查询元数据列表", description = "根据类型和关联ID查询元数据列表")
    public Result<List<MetadataDTO>> getMetadataByTypeAndRefId(
            @Parameter(description = "元数据类型") @PathVariable String type,
            @Parameter(description = "关联ID") @PathVariable String refId) {
        log.debug("根据类型和关联ID查询元数据列表, type: {}, refId: {}", type, refId);
        List<MetadataDTO> result = metadataService.getMetadataByTypeAndRefId(type, refId);
        return Result.success(result);
    }

    @GetMapping("/type/{type}/ref/{refId}/key/{metaKey}")
    @Operation(summary = "根据类型、关联ID和元数据键查询元数据", description = "根据类型、关联ID和元数据键查询单条元数据")
    public Result<MetadataDTO> getMetadataByTypeAndRefIdAndKey(
            @Parameter(description = "元数据类型") @PathVariable String type,
            @Parameter(description = "关联ID") @PathVariable String refId,
            @Parameter(description = "元数据键") @PathVariable String metaKey) {
        log.debug("根据类型、关联ID和元数据键查询元数据, type: {}, refId: {}, metaKey: {}", type, refId, metaKey);
        MetadataDTO result = metadataService.getMetadataByTypeAndRefIdAndKey(type, refId, metaKey);
        return Result.success(result);
    }

    @GetMapping("/search/key")
    @Operation(summary = "根据元数据键模糊查询", description = "根据元数据键模式模糊查询元数据列表")
    public Result<List<MetadataDTO>> searchMetadataByKey(
            @Parameter(description = "元数据类型") @RequestParam String type,
            @Parameter(description = "元数据键模式") @RequestParam String keyPattern) {
        log.debug("根据元数据键模糊查询, type: {}, keyPattern: {}", type, keyPattern);
        List<MetadataDTO> result = metadataService.searchMetadataByKey(type, keyPattern);
        return Result.success(result);
    }

    @GetMapping("/search/value")
    @Operation(summary = "根据元数据值模糊查询", description = "根据元数据值模式模糊查询元数据列表")
    public Result<List<MetadataDTO>> searchMetadataByValue(
            @Parameter(description = "元数据类型") @RequestParam String type,
            @Parameter(description = "元数据值模式") @RequestParam String valuePattern) {
        log.debug("根据元数据值模糊查询, type: {}, valuePattern: {}", type, valuePattern);
        List<MetadataDTO> result = metadataService.searchMetadataByValue(type, valuePattern);
        return Result.success(result);
    }

    @PutMapping
    @Operation(summary = "更新元数据", description = "更新单条元数据")
    public Result<MetadataDTO> updateMetadata(@Valid @RequestBody MetadataDTO metadataDTO) {
        log.debug("更新元数据: {}", metadataDTO);
        MetadataDTO result = metadataService.updateMetadata(metadataDTO);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "根据ID删除元数据", description = "根据ID删除单条元数据")
    public Result<Boolean> deleteMetadata(
            @Parameter(description = "元数据ID") @PathVariable Long id) {
        log.debug("根据ID删除元数据, id: {}", id);
        boolean result = metadataService.deleteMetadata(id);
        return Result.success(result);
    }

    @DeleteMapping("/type/{type}/ref/{refId}")
    @Operation(summary = "根据类型和关联ID删除元数据", description = "根据类型和关联ID删除元数据列表")
    public Result<Boolean> deleteMetadataByTypeAndRefId(
            @Parameter(description = "元数据类型") @PathVariable String type,
            @Parameter(description = "关联ID") @PathVariable String refId) {
        log.debug("根据类型和关联ID删除元数据, type: {}, refId: {}", type, refId);
        boolean result = metadataService.deleteMetadataByTypeAndRefId(type, refId);
        return Result.success(result);
    }

    @DeleteMapping("/type/{type}/ref/{refId}/key/{metaKey}")
    @Operation(summary = "根据类型、关联ID和元数据键删除元数据", description = "根据类型、关联ID和元数据键删除单条元数据")
    public Result<Boolean> deleteMetadataByTypeAndRefIdAndKey(
            @Parameter(description = "元数据类型") @PathVariable String type,
            @Parameter(description = "关联ID") @PathVariable String refId,
            @Parameter(description = "元数据键") @PathVariable String metaKey) {
        log.debug("根据类型、关联ID和元数据键删除元数据, type: {}, refId: {}, metaKey: {}", type, refId, metaKey);
        boolean result = metadataService.deleteMetadataByTypeAndRefIdAndKey(type, refId, metaKey);
        return Result.success(result);
    }
} 