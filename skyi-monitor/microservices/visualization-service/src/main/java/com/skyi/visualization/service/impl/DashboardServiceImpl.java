package com.skyi.visualization.service.impl;

import com.skyi.visualization.model.Dashboard;
import com.skyi.visualization.model.DashboardComponent;
import com.skyi.visualization.repository.DashboardComponentRepository;
import com.skyi.visualization.repository.DashboardRepository;
import com.skyi.visualization.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 仪表盘服务实现类
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;
    private final DashboardComponentRepository componentRepository;

    @Autowired
    public DashboardServiceImpl(DashboardRepository dashboardRepository,
                               DashboardComponentRepository componentRepository) {
        this.dashboardRepository = dashboardRepository;
        this.componentRepository = componentRepository;
    }

    @Override
    public Page<Dashboard> findAll(String name, Pageable pageable) {
        if (StringUtils.hasText(name)) {
            return dashboardRepository.findByNameContaining(name, pageable);
        }
        return dashboardRepository.findAll(pageable);
    }

    @Override
    public Optional<Dashboard> findById(Long id) {
        return dashboardRepository.findById(id);
    }

    @Override
    public Optional<Dashboard> getDefaultDashboard() {
        return dashboardRepository.findByIsDefaultTrue();
    }

    @Override
    @Transactional
    public Dashboard save(Dashboard dashboard) {
        // 如果设置为默认，需要取消之前的默认设置
        if (dashboard.getIsDefault()) {
            dashboardRepository.resetDefaultDashboard();
        }
        
        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        dashboard.setCreateTime(now);
        dashboard.setUpdateTime(now);
        
        return dashboardRepository.save(dashboard);
    }

    @Override
    @Transactional
    public Dashboard update(Long id, Dashboard dashboard) {
        Dashboard existingDashboard = dashboardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("仪表盘未找到: " + id));
        
        // 如果设置为默认，需要取消之前的默认设置
        if (dashboard.getIsDefault() && !existingDashboard.getIsDefault()) {
            dashboardRepository.resetDefaultDashboard();
        }
        
        // 更新基本信息
        existingDashboard.setName(dashboard.getName());
        existingDashboard.setTitle(dashboard.getTitle());
        existingDashboard.setDescription(dashboard.getDescription());
        existingDashboard.setLayout(dashboard.getLayout());
        existingDashboard.setIsShared(dashboard.getIsShared());
        existingDashboard.setIsDefault(dashboard.getIsDefault());
        existingDashboard.setUpdateTime(LocalDateTime.now());
        
        return dashboardRepository.save(existingDashboard);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Dashboard dashboard = dashboardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("仪表盘未找到: " + id));
        
        // 删除关联的组件
        componentRepository.deleteByDashboard(dashboard);
        
        // 删除仪表盘
        dashboardRepository.delete(dashboard);
    }

    @Override
    @Transactional
    public Dashboard setDefault(Long id) {
        Dashboard dashboard = dashboardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("仪表盘未找到: " + id));
        
        // 重置所有默认设置
        dashboardRepository.resetDefaultDashboard();
        
        // 设置新的默认仪表盘
        dashboard.setIsDefault(true);
        dashboard.setUpdateTime(LocalDateTime.now());
        
        return dashboardRepository.save(dashboard);
    }

    @Override
    public List<DashboardComponent> getComponents(Long dashboardId) {
        // 验证仪表盘是否存在
        Dashboard dashboard = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException("仪表盘未找到: " + dashboardId));
        
        return componentRepository.findByDashboard(dashboard);
    }

    @Override
    @Transactional
    public DashboardComponent saveComponent(Long dashboardId, DashboardComponent component) {
        // 验证仪表盘是否存在
        Dashboard dashboard = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException("仪表盘未找到: " + dashboardId));
        
        // 设置仪表盘关联
        component.setDashboard(dashboard);
        
        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        component.setCreateTime(now);
        component.setUpdateTime(now);
        
        return componentRepository.save(component);
    }

    @Override
    @Transactional
    public DashboardComponent updateComponent(Long dashboardId, Long componentId, DashboardComponent component) {
        // 验证仪表盘是否存在
        Dashboard dashboard = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException("仪表盘未找到: " + dashboardId));
        
        // 查找并验证组件
        DashboardComponent existingComponent = componentRepository.findById(componentId)
                .orElseThrow(() -> new EntityNotFoundException("组件未找到: " + componentId));
        
        // 验证组件是否属于该仪表盘
        if (!existingComponent.getDashboard().getId().equals(dashboardId)) {
            throw new IllegalArgumentException("组件不属于该仪表盘");
        }
        
        // 更新组件信息
        existingComponent.setTitle(component.getTitle());
        existingComponent.setType(component.getType());
        existingComponent.setPosition(component.getPosition());
        existingComponent.setDescription(component.getDescription());
        existingComponent.setDataSource(component.getDataSource());
        existingComponent.setStyles(component.getStyles());
        existingComponent.setRealtime(component.getRealtime());
        existingComponent.setRefreshInterval(component.getRefreshInterval());
        existingComponent.setTimeRange(component.getTimeRange());
        existingComponent.setSortOrder(component.getSortOrder());
        existingComponent.setUpdateTime(LocalDateTime.now());
        
        return componentRepository.save(existingComponent);
    }

    @Override
    @Transactional
    public void deleteComponent(Long dashboardId, Long componentId) {
        // 验证仪表盘是否存在
        if (!dashboardRepository.existsById(dashboardId)) {
            throw new EntityNotFoundException("仪表盘未找到: " + dashboardId);
        }
        
        // 查找并验证组件
        DashboardComponent component = componentRepository.findById(componentId)
                .orElseThrow(() -> new EntityNotFoundException("组件未找到: " + componentId));
        
        // 验证组件是否属于该仪表盘
        if (!component.getDashboard().getId().equals(dashboardId)) {
            throw new IllegalArgumentException("组件不属于该仪表盘");
        }
        
        // 删除组件
        componentRepository.delete(component);
    }
} 