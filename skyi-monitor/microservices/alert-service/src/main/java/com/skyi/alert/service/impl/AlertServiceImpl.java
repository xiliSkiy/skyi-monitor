package com.skyi.alert.service.impl;

import com.skyi.alert.dto.AlertDTO;
import com.skyi.alert.dto.ThresholdAlertDTO;
import com.skyi.alert.model.Alert;
import com.skyi.alert.model.AlertNotification;
import com.skyi.alert.model.AlertSeverity;
import com.skyi.alert.model.AlertStatus;
import com.skyi.alert.model.AlertType;
import com.skyi.alert.repository.AlertRepository;
import com.skyi.alert.service.AlertService;
import com.skyi.alert.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 告警服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {
    
    private final AlertRepository alertRepository;
    private final NotificationService notificationService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional
    public Alert processThresholdAlert(ThresholdAlertDTO thresholdAlertDTO) {
        log.info("处理阈值告警: {}", thresholdAlertDTO);
        
        // 创建新告警
        Alert alert = new Alert();
        alert.setAlertUuid(UUID.randomUUID().toString());
        alert.setName(thresholdAlertDTO.getMetricName() + " 超出阈值");
        alert.setMessage("指标 " + thresholdAlertDTO.getMetricName() + 
            " 当前值 " + thresholdAlertDTO.getValue() + 
            " 超出阈值 " + thresholdAlertDTO.getThreshold());
        alert.setMetricName(thresholdAlertDTO.getMetricName());
        alert.setMetricValue(thresholdAlertDTO.getValue());
        alert.setThreshold(thresholdAlertDTO.getThreshold());
        alert.setType(AlertType.THRESHOLD);
        alert.setSeverity(thresholdAlertDTO.getSeverity());
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setAssetId(thresholdAlertDTO.getAssetId());
        alert.setAssetName(thresholdAlertDTO.getAssetName());
        alert.setAssetType(thresholdAlertDTO.getAssetType());
        alert.setStartTime(LocalDateTime.now());
        alert.setNotified(false);
        alert.setNotificationCount(0);
        
        return alertRepository.save(alert);
    }
    
    @Override
    public Alert getAlertById(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("告警未找到，ID: " + id));
    }
    
    @Override
    public Alert getAlertByUuid(String uuid) {
        return alertRepository.findByAlertUuid(uuid);
    }
    
    @Override
    public Page<Alert> getAlerts(List<AlertStatus> statuses, Pageable pageable) {
        if (statuses == null || statuses.isEmpty()) {
            return alertRepository.findAll(pageable);
        }
        return alertRepository.findByStatusIn(statuses, pageable);
    }
    
    @Override
    public Page<Alert> getAlertsByAssetId(Long assetId, List<AlertStatus> statuses, Pageable pageable) {
        if (statuses == null || statuses.isEmpty()) {
            return alertRepository.findByAssetId(assetId, pageable);
        }
        return alertRepository.findByAssetIdAndStatusIn(assetId, statuses, pageable);
    }
    
    @Override
    @Transactional
    public Alert createAlert(Alert alert) {
        // 设置创建时间和初始状态
        alert.setStartTime(LocalDateTime.now());
        if (alert.getStatus() == null) {
            alert.setStatus(AlertStatus.ACTIVE);
        }
        
        Alert savedAlert = alertRepository.save(alert);
        log.info("创建新告警，ID: {}, 资产: {}, 严重程度: {}", 
                savedAlert.getId(), savedAlert.getAssetId(), savedAlert.getSeverity());
                
        // 处理告警通知
        processAlertNotification(savedAlert.getId());
        
        return savedAlert;
    }
    
    @Override
    @Transactional
    public Alert acknowledgeAlert(Long id, String acknowledgedBy) {
        Alert alert = getAlertById(id);
        
        // 如果已经是已确认或已解决状态，无需更改
        if (alert.getStatus() == AlertStatus.ACKNOWLEDGED || alert.getStatus() == AlertStatus.RESOLVED) {
            return alert;
        }
        
        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedBy(acknowledgedBy);
        alert.setUpdateTime(LocalDateTime.now());
        
        Alert updatedAlert = alertRepository.save(alert);
        log.info("确认告警，ID: {}, 确认人: {}", id, acknowledgedBy);
        
        return updatedAlert;
    }
    
    @Override
    @Transactional
    public Alert resolveAlert(Long id, String resolvedBy, String comment) {
        Alert alert = getAlertById(id);
        
        // 如果已经是已解决状态，无需更改
        if (alert.getStatus() == AlertStatus.RESOLVED) {
            return alert;
        }
        
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedBy(resolvedBy);
        alert.setEndTime(LocalDateTime.now());
        
        Alert updatedAlert = alertRepository.save(alert);
        log.info("解决告警，ID: {}, 解决人: {}", id, resolvedBy);
        
        return updatedAlert;
    }
    
    @Override
    @Transactional
    public boolean closeAlert(Long alertId) {
        Alert alert = getAlertById(alertId);
        if (alert == null) {
            return false;
        }
        
        alert.setStatus(AlertStatus.CLOSED);
        alert.setEndTime(LocalDateTime.now());
        alertRepository.save(alert);
        
        return true;
    }
    
    @Override
    public List<Alert> findAlertsForNotification() {
        return alertRepository.findByStatusInAndNotifiedFalse(List.of(AlertStatus.ACTIVE));
    }
    
    @Override
    @Transactional
    public Alert updateAlertNotificationStatus(Long alertId, boolean notified) {
        Alert alert = getAlertById(alertId);
        if (alert == null) {
            return null;
        }
        
        alert.setNotified(notified);
        alert.setLastNotifiedTime(LocalDateTime.now());
        if (alert.getNotificationCount() == null) {
            alert.setNotificationCount(1);
        } else {
            alert.setNotificationCount(alert.getNotificationCount() + 1);
        }
        
        return alertRepository.save(alert);
    }
    
    @Override
    public Map<AlertSeverity, Long> countActiveAlertsBySeverity() {
        List<AlertStatus> activeStatuses = Arrays.asList(AlertStatus.ACTIVE, AlertStatus.ACKNOWLEDGED);
        
        List<Alert> activeAlerts = alertRepository.findByStatusIn(activeStatuses, null).getContent();
        
        Map<AlertSeverity, Long> countsMap = new HashMap<>();
        for (AlertSeverity severity : AlertSeverity.values()) {
            countsMap.put(severity, 0L);
        }
        
        for (Alert alert : activeAlerts) {
            AlertSeverity severity = alert.getSeverity();
            countsMap.put(severity, countsMap.getOrDefault(severity, 0L) + 1L);
        }
        
        return countsMap;
    }
    
    @Override
    public Map<String, Long> countActiveAlertsByAssetType() {
        List<AlertStatus> activeStatuses = Arrays.asList(AlertStatus.ACTIVE, AlertStatus.ACKNOWLEDGED);
        
        List<Alert> activeAlerts = alertRepository.findByStatusIn(activeStatuses, null).getContent();
        
        Map<String, Long> countsMap = new HashMap<>();
        
        for (Alert alert : activeAlerts) {
            String assetType = alert.getAssetType();
            if (assetType == null) {
                assetType = "未知";
            }
            countsMap.put(assetType, countsMap.getOrDefault(assetType, 0L) + 1L);
        }
        
        return countsMap;
    }
    
    @Override
    public Map<String, Long> countAlertsByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        List<Alert> allAlerts = alertRepository.findAll();
        
        List<Alert> alerts = allAlerts.stream()
                .filter(alert -> {
                    LocalDateTime alertTime = alert.getStartTime();
                    return alertTime != null && 
                           !alertTime.isBefore(startTime) && 
                           !alertTime.isAfter(endTime);
                })
                .collect(Collectors.toList());
        
        Map<String, Long> dailyCounts = alerts.stream()
                .collect(Collectors.groupingBy(
                        alert -> alert.getStartTime().format(DATE_FORMATTER),
                        Collectors.counting()
                ));
        
        Map<String, Long> result = new TreeMap<>();
        LocalDateTime current = startTime;
        while (!current.isAfter(endTime)) {
            String dateStr = current.format(DATE_FORMATTER);
            result.put(dateStr, dailyCounts.getOrDefault(dateStr, 0L));
            current = current.plusDays(1);
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public int processAlertEscalation() {
        // 查找需要升级的告警
        List<Alert> alertsToEscalate = alertRepository.findAlertsForEscalation();
        
        int processed = 0;
        for (Alert alert : alertsToEscalate) {
            // 根据当前级别提升告警级别
            if (alert.getSeverity() != AlertSeverity.CRITICAL) {
                // 提升级别
                AlertSeverity currentSeverity = alert.getSeverity();
                AlertSeverity newSeverity = getNextSeverity(currentSeverity);
                
                alert.setSeverity(newSeverity);
                alert.setNotified(false); // 标记为需要重新通知
                
                alertRepository.save(alert);
                processed++;
                
                log.info("告警已升级: ID={}, 级别: {} -> {}", 
                    alert.getId(), currentSeverity, newSeverity);
            }
        }
        
        return processed;
    }
    
    /**
     * 获取下一个更高级别的告警级别
     */
    private AlertSeverity getNextSeverity(AlertSeverity current) {
        switch (current) {
            case INFO:
                return AlertSeverity.WARNING;
            case WARNING:
                return AlertSeverity.CRITICAL;
            default:
                return AlertSeverity.CRITICAL;
        }
    }
    
    @Override
    @Transactional
    public void processAlertNotification(Long alertId) {
        Alert alert = getAlertById(alertId);
        
        notificationService.sendAlertNotifications(alert);
        
        log.info("已处理告警通知，告警ID: {}", alertId);
    }

    @Override
    public List<AlertNotification> getAlertNotifications(Long alertId) {
        // 使用NotificationService获取告警的通知记录
        return notificationService.getNotificationsByAlertId(alertId);
    }
} 