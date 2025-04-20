package com.skyi.visualization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 仪表盘DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private Long id;
    private String name;
    private String title;
    private String description;
    private String layout;
    private List<DashboardComponentDTO> components;
    private Boolean isDefault;
    private Boolean isShared;
    private Long ownerId;
    private String ownerName;
    private Integer refreshInterval;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 