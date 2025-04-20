package com.skyi.alert.controller;

import com.skyi.alert.dto.AlertDTO;
import com.skyi.alert.dto.ResponseResult;
import com.skyi.alert.model.Alert;
import com.skyi.alert.model.AlertNotification;
import com.skyi.alert.model.AlertSeverity;
import com.skyi.alert.model.AlertStatus;
import com.skyi.alert.service.AlertService;
import com.skyi.alert.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alert")
@Tag(name = "告警管理", description = "告警相关接口")
public class AlertController {

    private static final Logger logger = LoggerFactory.getLogger(AlertController.class);

    @Autowired
    private AlertService alertService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/alerts")
    @Operation(summary = "获取告警列表", description = "分页查询告警列表，支持按状态过滤")
    public ResponseResult<Map<String, Object>> getAlerts(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "告警状态，多个状态用逗号分隔") @RequestParam(required = false) String status
    ) {
        try {
            List<AlertStatus> statuses = new ArrayList<>();
            if (status != null && !status.isEmpty()) {
                String[] statusArray = status.split(",");
                for (String s : statusArray) {
                    try {
                        AlertStatus alertStatus = AlertStatus.valueOf(s.toUpperCase());
                        statuses.add(alertStatus);
                    } catch (IllegalArgumentException e) {
                        logger.warn("无效的告警状态: {}", s);
                    }
                }
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));
            Page<Alert> alertPage = alertService.getAlerts(statuses, pageable);

            Map<String, Object> result = new HashMap<>();
            result.put("content", alertPage.getContent());
            result.put("totalElements", alertPage.getTotalElements());
            result.put("totalPages", alertPage.getTotalPages());
            result.put("number", alertPage.getNumber());
            result.put("size", alertPage.getSize());

            return ResponseResult.success(result);
        } catch (Exception e) {
            logger.error("获取告警列表失败", e);
            return ResponseResult.error("获取告警列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/alerts/{id}")
    @Operation(summary = "获取告警详情", description = "根据告警ID获取详情")
    public ResponseResult<Alert> getAlertDetail(
            @Parameter(description = "告警ID") @PathVariable Long id
    ) {
        try {
            Alert alert = alertService.getAlertById(id);
            if (alert != null) {
                return ResponseResult.success(alert);
            } else {
                return ResponseResult.error("未找到告警: " + id);
            }
        } catch (Exception e) {
            logger.error("获取告警详情失败", e);
            return ResponseResult.error("获取告警详情失败: " + e.getMessage());
        }
    }

    @PostMapping("/alerts/{id}/acknowledge")
    @Operation(summary = "确认告警", description = "将告警状态更新为已确认")
    public ResponseResult<Alert> acknowledgeAlert(
            @Parameter(description = "告警ID") @PathVariable Long id,
            @Parameter(description = "确认人信息") @RequestBody Map<String, String> data
    ) {
        try {
            String acknowledgedBy = data.getOrDefault("acknowledgedBy", "system");
            Alert alert = alertService.acknowledgeAlert(id, acknowledgedBy);
            if (alert != null) {
                return ResponseResult.success(alert);
            } else {
                return ResponseResult.error("未找到告警: " + id);
            }
        } catch (Exception e) {
            logger.error("确认告警失败", e);
            return ResponseResult.error("确认告警失败: " + e.getMessage());
        }
    }

    @PostMapping("/alerts/{id}/resolve")
    @Operation(summary = "解决告警", description = "将告警状态更新为已解决")
    public ResponseResult<Alert> resolveAlert(
            @Parameter(description = "告警ID") @PathVariable Long id,
            @Parameter(description = "解决信息") @RequestBody Map<String, String> data
    ) {
        try {
            String resolvedBy = data.getOrDefault("resolvedBy", "system");
            String comment = data.getOrDefault("comment", "");
            Alert alert = alertService.resolveAlert(id, resolvedBy, comment);
            if (alert != null) {
                return ResponseResult.success(alert);
            } else {
                return ResponseResult.error("未找到告警: " + id);
            }
        } catch (Exception e) {
            logger.error("解决告警失败", e);
            return ResponseResult.error("解决告警失败: " + e.getMessage());
        }
    }

    @GetMapping("/assets/{assetId}/alerts")
    @Operation(summary = "获取资产告警", description = "分页查询某个资产的告警列表")
    public ResponseResult<Map<String, Object>> getAssetAlerts(
            @Parameter(description = "资产ID") @PathVariable Long assetId,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "告警状态，多个状态用逗号分隔") @RequestParam(required = false) String status
    ) {
        try {
            List<AlertStatus> statuses = new ArrayList<>();
            if (status != null && !status.isEmpty()) {
                String[] statusArray = status.split(",");
                for (String s : statusArray) {
                    try {
                        AlertStatus alertStatus = AlertStatus.valueOf(s.toUpperCase());
                        statuses.add(alertStatus);
                    } catch (IllegalArgumentException e) {
                        logger.warn("无效的告警状态: {}", s);
                    }
                }
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));
            Page<Alert> alertPage = alertService.getAlertsByAssetId(assetId, statuses, pageable);

            Map<String, Object> result = new HashMap<>();
            result.put("content", alertPage.getContent());
            result.put("totalElements", alertPage.getTotalElements());
            result.put("totalPages", alertPage.getTotalPages());
            result.put("number", alertPage.getNumber());
            result.put("size", alertPage.getSize());

            return ResponseResult.success(result);
        } catch (Exception e) {
            logger.error("获取资产告警列表失败", e);
            return ResponseResult.error("获取资产告警列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "获取告警统计信息", description = "获取告警总体统计信息")
    public ResponseResult<Map<String, Object>> getAlertStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            Map<AlertSeverity, Long> severityStats = alertService.countActiveAlertsBySeverity();
            
            // 计算活跃告警总数
            long totalActive = severityStats.values().stream().mapToLong(Long::longValue).sum();
            stats.put("totalActive", totalActive);
            stats.put("bySeverity", severityStats);
            
            return ResponseResult.success(stats);
        } catch (Exception e) {
            logger.error("获取告警统计信息失败", e);
            return ResponseResult.error("获取告警统计信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats/severity")
    @Operation(summary = "按级别统计告警", description = "按告警级别统计活跃告警数量")
    public ResponseResult<Map<String, Long>> getAlertStatsBySeverity() {
        try {
            Map<AlertSeverity, Long> severityStats = alertService.countActiveAlertsBySeverity();
            
            // 转换枚举键为字符串
            Map<String, Long> result = new HashMap<>();
            for (Map.Entry<AlertSeverity, Long> entry : severityStats.entrySet()) {
                result.put(entry.getKey().name(), entry.getValue());
            }
            
            return ResponseResult.success(result);
        } catch (Exception e) {
            logger.error("按级别统计告警失败", e);
            return ResponseResult.error("按级别统计告警失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats/asset-type")
    @Operation(summary = "按资产类型统计告警", description = "按资产类型统计活跃告警数量")
    public ResponseResult<Map<String, Long>> getAlertStatsByAssetType() {
        try {
            Map<String, Long> assetTypeStats = alertService.countActiveAlertsByAssetType();
            return ResponseResult.success(assetTypeStats);
        } catch (Exception e) {
            logger.error("按资产类型统计告警失败", e);
            return ResponseResult.error("按资产类型统计告警失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats/date-range")
    @Operation(summary = "按日期范围统计告警", description = "按日期范围统计告警数量")
    public ResponseResult<Map<String, Long>> getAlertStatsByDateRange(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd") @RequestParam String startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd") @RequestParam String endDate
    ) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
            
            Map<String, Long> dateStats = alertService.countAlertsByDateRange(start, end);
            return ResponseResult.success(dateStats);
        } catch (Exception e) {
            logger.error("按日期范围统计告警失败", e);
            return ResponseResult.error("按日期范围统计告警失败: " + e.getMessage());
        }
    }

    @GetMapping("/alerts/{alertId}/notifications")
    @Operation(summary = "获取告警通知记录", description = "获取指定告警的通知记录")
    public ResponseResult<List<AlertNotification>> getAlertNotifications(
            @Parameter(description = "告警ID") @PathVariable Long alertId
    ) {
        try {
            List<AlertNotification> notifications = notificationService.getNotificationsByAlertId(alertId);
            return ResponseResult.success(notifications);
        } catch (Exception e) {
            logger.error("获取告警通知记录失败", e);
            return ResponseResult.error("获取告警通知记录失败: " + e.getMessage());
        }
    }
} 