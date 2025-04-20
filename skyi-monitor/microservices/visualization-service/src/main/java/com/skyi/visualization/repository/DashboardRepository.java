package com.skyi.visualization.repository;

import com.skyi.visualization.model.Dashboard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 仪表盘仓库接口
 */
public interface DashboardRepository extends JpaRepository<Dashboard, Long> {

    /**
     * 根据名称查找仪表盘
     */
    Optional<Dashboard> findByName(String name);
    
    /**
     * 根据所有者ID查找仪表盘列表
     */
    List<Dashboard> findByOwnerId(Long ownerId);
    
    /**
     * 根据所有者ID分页查询仪表盘
     */
    Page<Dashboard> findByOwnerId(Long ownerId, Pageable pageable);
    
    /**
     * 查询默认仪表盘
     */
    Optional<Dashboard> findByIsDefaultTrue();
    
    /**
     * 查询共享仪表盘列表
     */
    List<Dashboard> findByIsSharedTrue();
    
    /**
     * 分页查询共享仪表盘
     */
    Page<Dashboard> findByIsSharedTrue(Pageable pageable);
    
    /**
     * 分页查询用户可访问的仪表盘（包括自己的和共享的）
     */
    @Query("SELECT d FROM Dashboard d WHERE d.ownerId = :ownerId OR d.isShared = true")
    Page<Dashboard> findAccessibleDashboards(@Param("ownerId") Long ownerId, Pageable pageable);
    
    /**
     * 按名称模糊查询仪表盘
     */
    Page<Dashboard> findByNameContaining(String name, Pageable pageable);
    
    /**
     * 按名称或标题或描述模糊查询仪表盘
     */
    @Query("SELECT d FROM Dashboard d WHERE d.name LIKE %:keyword% OR d.title LIKE %:keyword% OR d.description LIKE %:keyword%")
    Page<Dashboard> searchDashboards(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 重置所有仪表盘的默认状态
     */
    @Modifying
    @Query("UPDATE Dashboard d SET d.isDefault = false WHERE d.isDefault = true")
    void resetDefaultDashboard();
} 