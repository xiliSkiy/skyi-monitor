package com.skyi.visualization.service;

import com.skyi.visualization.model.Dashboard;
import com.skyi.visualization.model.DashboardComponent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 仪表盘服务接口
 */
public interface DashboardService {

    /**
     * 查询所有仪表盘
     * @param name 名称过滤条件（可选）
     * @param pageable 分页参数
     * @return 仪表盘分页结果
     */
    Page<Dashboard> findAll(String name, Pageable pageable);

    /**
     * 根据ID查询仪表盘
     * @param id 仪表盘ID
     * @return 仪表盘信息
     */
    Optional<Dashboard> findById(Long id);

    /**
     * 获取默认仪表盘
     * @return 默认仪表盘
     */
    Optional<Dashboard> getDefaultDashboard();

    /**
     * 保存仪表盘
     * @param dashboard 仪表盘实体
     * @return 保存后的仪表盘
     */
    Dashboard save(Dashboard dashboard);

    /**
     * 更新仪表盘
     * @param id 仪表盘ID
     * @param dashboard 更新的仪表盘信息
     * @return 更新后的仪表盘
     */
    Dashboard update(Long id, Dashboard dashboard);

    /**
     * 删除仪表盘
     * @param id 仪表盘ID
     */
    void delete(Long id);

    /**
     * 设置默认仪表盘
     * @param id 仪表盘ID
     * @return 设置为默认的仪表盘
     */
    Dashboard setDefault(Long id);

    /**
     * 获取仪表盘组件
     * @param dashboardId 仪表盘ID
     * @return 组件列表
     */
    List<DashboardComponent> getComponents(Long dashboardId);

    /**
     * 保存仪表盘组件
     * @param dashboardId 仪表盘ID
     * @param component 组件信息
     * @return 保存后的组件
     */
    DashboardComponent saveComponent(Long dashboardId, DashboardComponent component);

    /**
     * 更新仪表盘组件
     * @param dashboardId 仪表盘ID
     * @param componentId 组件ID
     * @param component 更新的组件信息
     * @return 更新后的组件
     */
    DashboardComponent updateComponent(Long dashboardId, Long componentId, DashboardComponent component);

    /**
     * 删除仪表盘组件
     * @param dashboardId 仪表盘ID
     * @param componentId 组件ID
     */
    void deleteComponent(Long dashboardId, Long componentId);
} 