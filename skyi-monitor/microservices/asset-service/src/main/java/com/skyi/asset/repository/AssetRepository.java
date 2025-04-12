package com.skyi.asset.repository;

import com.skyi.asset.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * 资产数据访问接口
 */
public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {
    
    /**
     * 根据资产编码查询资产
     *
     * @param code 资产编码
     * @return 资产信息
     */
    Optional<Asset> findByCode(String code);
    
    /**
     * 根据资产类型查询资产列表
     *
     * @param type 资产类型
     * @return 资产列表
     */
    List<Asset> findByType(String type);
    
    /**
     * 根据状态查询资产列表
     *
     * @param status 状态
     * @return 资产列表
     */
    List<Asset> findByStatus(Integer status);
    
    /**
     * 根据IP地址查询资产
     *
     * @param ipAddress IP地址
     * @return 资产信息
     */
    Optional<Asset> findByIpAddress(String ipAddress);
    
    /**
     * 根据部门查询资产列表
     *
     * @param department 部门
     * @return 资产列表
     */
    List<Asset> findByDepartment(String department);
    
    /**
     * 根据负责人查询资产列表
     *
     * @param owner 负责人
     * @return 资产列表
     */
    List<Asset> findByOwner(String owner);
} 