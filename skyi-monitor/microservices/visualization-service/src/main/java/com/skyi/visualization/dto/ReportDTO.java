package com.skyi.visualization.dto;

import com.skyi.visualization.model.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 报表DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
    private Long id;
    private String name;
    private String title;
    private String description;
    private ReportType type;
    private String template;
    private String parameters;
    private String schedule;
    private Boolean enabled;
    private LocalDateTime lastGeneratedTime;
    private String lastGeneratedStatus;
    private String reportPath;
    private Long creatorId;
    private String creatorName;
    private String recipients;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 