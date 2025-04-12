package com.skyi.asset.controller;

import com.skyi.asset.dto.AssetDTO;
import com.skyi.asset.service.AssetService;
import com.skyi.common.model.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资产管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {
    
    private final AssetService assetService;
    
    /**
     * 创建资产
     *
     * @param assetDTO 资产信息
     * @return 创建结果
     */
    @PostMapping
    public Result<AssetDTO> createAsset(@RequestBody @Validated AssetDTO assetDTO) {
        log.info("创建资产请求：{}", assetDTO.getCode());
        AssetDTO created = assetService.createAsset(assetDTO);
        return Result.success(created);
    }
    
    /**
     * 更新资产
     *
     * @param id 资产ID
     * @param assetDTO 资产信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<AssetDTO> updateAsset(@PathVariable Long id, @RequestBody @Validated AssetDTO assetDTO) {
        log.info("更新资产请求：id={}", id);
        AssetDTO updated = assetService.updateAsset(id, assetDTO);
        return Result.success(updated);
    }
    
    /**
     * 删除资产
     *
     * @param id 资产ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAsset(@PathVariable Long id) {
        log.info("删除资产请求：id={}", id);
        assetService.deleteAsset(id);
        return Result.success();
    }
    
    /**
     * 根据ID查询资产
     *
     * @param id 资产ID
     * @return 资产信息
     */
    @GetMapping("/{id}")
    public Result<AssetDTO> getAssetById(@PathVariable Long id) {
        log.info("查询资产请求：id={}", id);
        AssetDTO asset = assetService.getAssetById(id);
        return Result.success(asset);
    }
    
    /**
     * 根据编码查询资产
     *
     * @param code 资产编码
     * @return 资产信息
     */
    @GetMapping("/code/{code}")
    public Result<AssetDTO> getAssetByCode(@PathVariable String code) {
        log.info("查询资产请求：code={}", code);
        AssetDTO asset = assetService.getAssetByCode(code);
        return Result.success(asset);
    }
    
    /**
     * 分页查询资产列表
     *
     * @param name 资产名称（可选）
     * @param type 资产类型（可选）
     * @param status 状态（可选）
     * @param page 页码（从0或1开始）
     * @param size 每页大小
     * @return 资产分页列表
     */
    @GetMapping
    public Result<Page<AssetDTO>> listAssets(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("分页查询资产请求：name={}, type={}, status={}, page={}, size={}", name, type, status, page, size);
        // 兼容前端页码从0或1开始的情况
        int pageIndex = page;
        if (page > 0) {
            pageIndex = page - 1;
        }
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "updateTime"));
        Page<AssetDTO> assetPage = assetService.listAssets(name, type, status, pageable);
        return Result.success(assetPage);
    }
    
    /**
     * 根据类型查询资产列表
     *
     * @param type 资产类型
     * @return 资产列表
     */
    @GetMapping("/type/{type}")
    public Result<List<AssetDTO>> listAssetsByType(@PathVariable String type) {
        log.info("查询资产请求：type={}", type);
        List<AssetDTO> assets = assetService.listAssetsByType(type);
        return Result.success(assets);
    }
    
    /**
     * 根据部门查询资产列表
     *
     * @param department 部门
     * @return 资产列表
     */
    @GetMapping("/department/{department}")
    public Result<List<AssetDTO>> listAssetsByDepartment(@PathVariable String department) {
        log.info("查询资产请求：department={}", department);
        List<AssetDTO> assets = assetService.listAssetsByDepartment(department);
        return Result.success(assets);
    }
    
    /**
     * 根据负责人查询资产列表
     *
     * @param owner 负责人
     * @return 资产列表
     */
    @GetMapping("/owner/{owner}")
    public Result<List<AssetDTO>> listAssetsByOwner(@PathVariable String owner) {
        log.info("查询资产请求：owner={}", owner);
        List<AssetDTO> assets = assetService.listAssetsByOwner(owner);
        return Result.success(assets);
    }
} 