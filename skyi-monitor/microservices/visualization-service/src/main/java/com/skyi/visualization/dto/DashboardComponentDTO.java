package com.skyi.visualization.dto;

import com.skyi.visualization.model.ComponentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仪表盘组件DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardComponentDTO {
    private Long id;
    private Long dashboardId;
    private ComponentType type;
    private String title;
    private String description;
    private String position;
    private String dataSource;
    private String styles;
    private Boolean realtime;
    private Integer refreshInterval;
    private Integer timeRange;
    private Integer sortOrder;
} 