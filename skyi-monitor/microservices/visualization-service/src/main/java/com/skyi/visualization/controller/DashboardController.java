package com.skyi.visualization.controller;

import com.skyi.visualization.model.Dashboard;
import com.skyi.visualization.model.DashboardComponent;
import com.skyi.visualization.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘控制器
 */
@RestController
@RequestMapping("/api/visualization/dashboards")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 健康检查接口
     * @return 服务状态信息
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "visualization-service");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 分页查询仪表盘列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param name 仪表盘名称（可选）
     * @param title 仪表盘标题（可选）
     * @return 分页结果
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboards(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String title) {
        
        // 创建分页请求
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "updateTime"));
        
        // 查询仪表盘列表
        Page<Dashboard> dashboardPage = dashboardService.findAll(name, pageable);
        
        // 构建响应
        Map<String, Object> response = new HashMap<>();
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", dashboardPage.getContent());
        data.put("total", dashboardPage.getTotalElements());
        data.put("pages", dashboardPage.getTotalPages());
        
        response.put("code", 200);
        response.put("message", "成功");
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取仪表盘详情
     * @param id 仪表盘ID
     * @return 仪表盘详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDashboard(@PathVariable Long id) {
        return dashboardService.findById(id)
                .map(dashboard -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", 200);
                    response.put("message", "成功");
                    response.put("data", dashboard);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取默认仪表盘
     * @return 默认仪表盘
     */
    @GetMapping("/default")
    public ResponseEntity<Map<String, Object>> getDefaultDashboard() {
        return dashboardService.getDefaultDashboard()
                .map(dashboard -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", 200);
                    response.put("message", "成功");
                    response.put("data", dashboard);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 创建仪表盘
     * @param dashboard 仪表盘信息
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createDashboard(@RequestBody Dashboard dashboard) {
        Dashboard saved = dashboardService.save(dashboard);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "创建成功");
        response.put("data", saved);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新仪表盘
     * @param id 仪表盘ID
     * @param dashboard 更新的仪表盘信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateDashboard(
            @PathVariable Long id, @RequestBody Dashboard dashboard) {
        Dashboard updated = dashboardService.update(id, dashboard);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "更新成功");
        response.put("data", updated);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除仪表盘
     * @param id 仪表盘ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteDashboard(@PathVariable Long id) {
        dashboardService.delete(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "删除成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 设置默认仪表盘
     * @param id 仪表盘ID
     * @return 设置结果
     */
    @PutMapping("/{id}/default")
    public ResponseEntity<Map<String, Object>> setDefaultDashboard(@PathVariable Long id) {
        Dashboard dashboard = dashboardService.setDefault(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "设置默认仪表盘成功");
        response.put("data", dashboard);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取仪表盘组件列表
     * @param dashboardId 仪表盘ID
     * @return 组件列表
     */
    @GetMapping("/{dashboardId}/components")
    public ResponseEntity<Map<String, Object>> getComponents(@PathVariable Long dashboardId) {
        List<DashboardComponent> components = dashboardService.getComponents(dashboardId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "成功");
        response.put("data", components);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 创建仪表盘组件
     * @param dashboardId 仪表盘ID
     * @param component 组件信息
     * @return 创建结果
     */
    @PostMapping("/{dashboardId}/components")
    public ResponseEntity<Map<String, Object>> createComponent(
            @PathVariable Long dashboardId, @RequestBody DashboardComponent component) {
        DashboardComponent saved = dashboardService.saveComponent(dashboardId, component);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "创建成功");
        response.put("data", saved);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新仪表盘组件
     * @param dashboardId 仪表盘ID
     * @param componentId 组件ID
     * @param component 更新的组件信息
     * @return 更新结果
     */
    @PutMapping("/{dashboardId}/components/{componentId}")
    public ResponseEntity<Map<String, Object>> updateComponent(
            @PathVariable Long dashboardId, 
            @PathVariable Long componentId, 
            @RequestBody DashboardComponent component) {
        DashboardComponent updated = dashboardService.updateComponent(dashboardId, componentId, component);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "更新成功");
        response.put("data", updated);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除仪表盘组件
     * @param dashboardId 仪表盘ID
     * @param componentId 组件ID
     * @return 删除结果
     */
    @DeleteMapping("/{dashboardId}/components/{componentId}")
    public ResponseEntity<Map<String, Object>> deleteComponent(
            @PathVariable Long dashboardId, @PathVariable Long componentId) {
        dashboardService.deleteComponent(dashboardId, componentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "删除成功");
        
        return ResponseEntity.ok(response);
    }
} 