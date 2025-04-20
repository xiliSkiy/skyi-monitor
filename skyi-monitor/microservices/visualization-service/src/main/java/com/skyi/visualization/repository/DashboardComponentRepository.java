package com.skyi.visualization.repository;

import com.skyi.visualization.model.ComponentType;
import com.skyi.visualization.model.Dashboard;
import com.skyi.visualization.model.DashboardComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 仪表盘组件仓库接口
 */
public interface DashboardComponentRepository extends JpaRepository<DashboardComponent, Long> {

    /**
     * 根据仪表盘ID查询组件列表
     */
    List<DashboardComponent> findByDashboardId(Long dashboardId);
    
    /**
     * 根据仪表盘实体查询组件列表
     */
    List<DashboardComponent> findByDashboard(Dashboard dashboard);
    
    /**
     * 根据仪表盘ID和组件类型查询组件列表
     */
    List<DashboardComponent> findByDashboardIdAndType(Long dashboardId, ComponentType type);
    
    /**
     * 查询实时数据组件列表
     */
    List<DashboardComponent> findByRealtimeTrue();
    
    /**
     * 根据仪表盘ID删除所有组件
     */
    void deleteByDashboardId(Long dashboardId);
    
    /**
     * 根据仪表盘实体删除所有组件
     */
    void deleteByDashboard(Dashboard dashboard);
    
    /**
     * 根据仪表盘ID查询组件数量
     */
    @Query("SELECT COUNT(c) FROM DashboardComponent c WHERE c.dashboard.id = :dashboardId")
    int countByDashboardId(@Param("dashboardId") Long dashboardId);
    
    /**
     * 根据仪表盘ID查询组件列表，按排序号排序
     */
    @Query("SELECT c FROM DashboardComponent c WHERE c.dashboard.id = :dashboardId ORDER BY c.sortOrder ASC")
    List<DashboardComponent> findByDashboardIdOrderBySortOrder(@Param("dashboardId") Long dashboardId);
} 