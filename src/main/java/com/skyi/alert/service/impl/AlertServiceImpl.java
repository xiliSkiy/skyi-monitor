package com.skyi.alert.service.impl;

import com.skyi.alert.dto.ThresholdAlertDTO;
import com.skyi.alert.model.Alert;
import com.skyi.alert.model.AlertSeverity;
import com.skyi.alert.model.AlertStatus;
import com.skyi.alert.model.AlertType;
import com.skyi.alert.repository.AlertRepository;
import com.skyi.alert.service.AlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 告警服务实现类
 */
@Slf4j
@Service
public class AlertServiceImpl implements AlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Override
    @Transactional
    public Alert processThresholdAlert(ThresholdAlertDTO thresholdAlertDTO) {
        log.info("处理阈值告警: assetId={}, metricName={}, value={}, threshold={}",
                thresholdAlertDTO.getAssetId(), thresholdAlertDTO.getMetricName(),
                thresholdAlertDTO.getValue(), thresholdAlertDTO.getThreshold());

        // 生成告警UUID
        String alertUuid = UUID.randomUUID().toString();

        // 创建告警对象
        Alert alert = new Alert();
        alert.setAlertUuid(alertUuid);
        alert.setType(AlertType.THRESHOLD);
        alert.setName(thresholdAlertDTO.getName());
        alert.setSeverity(thresholdAlertDTO.getSeverity());
        alert.setAssetId(thresholdAlertDTO.getAssetId());
        alert.setAssetName(thresholdAlertDTO.getAssetName());
        alert.setAssetType(thresholdAlertDTO.getAssetType());
        alert.setMetricName(thresholdAlertDTO.getMetricName());
        alert.setMetricValue(thresholdAlertDTO.getValue());
        alert.setThreshold(thresholdAlertDTO.getThreshold());
        alert.setMessage(thresholdAlertDTO.getMessage());
        alert.setStartTime(LocalDateTime.now());
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setNotified(false);
        alert.setNotificationCount(0);
        alert.setCreateTime(LocalDateTime.now());
        alert.setUpdateTime(LocalDateTime.now());

        // 设置标签
        if (thresholdAlertDTO.getTags() != null && !thresholdAlertDTO.getTags().isEmpty()) {
            alert.setTags(String.join(",", thresholdAlertDTO.getTags()));
        }

        // 保存告警
        return alertRepository.save(alert);
    }

    @Override
    public Alert getAlertById(Long id) {
        return alertRepository.findById(id).orElse(null);
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
    public Alert acknowledgeAlert(Long alertId, String acknowledgedBy) {
        Alert alert = getAlertById(alertId);
        if (alert == null) {
            return null;
        }

        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedBy(acknowledgedBy);
        alert.setAcknowledgedTime(LocalDateTime.now());
        alert.setUpdateTime(LocalDateTime.now());

        return alertRepository.save(alert);
    }

    @Override
    @Transactional
    public Alert resolveAlert(Long alertId, String resolvedBy, String comment) {
        Alert alert = getAlertById(alertId);
        if (alert == null) {
            return null;
        }

        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedBy(resolvedBy);
        alert.setResolveComment(comment);
        alert.setResolvedTime(LocalDateTime.now());
        alert.setEndTime(LocalDateTime.now());
        alert.setUpdateTime(LocalDateTime.now());

        return alertRepository.save(alert);
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
        alert.setUpdateTime(LocalDateTime.now());

        alertRepository.save(alert);
        return true;
    }

    @Override
    public List<Alert> findAlertsForNotification() {
        return alertRepository.findByStatusInAndNotifiedFalse(Arrays.asList(AlertStatus.ACTIVE, AlertStatus.ACKNOWLEDGED));
    }

    @Override
    @Transactional
    public Alert updateAlertNotificationStatus(Long alertId, boolean notified) {
        Alert alert = getAlertById(alertId);
        if (alert == null) {
            return null;
        }

        alert.setNotified(notified);
        if (notified) {
            alert.setLastNotifiedTime(LocalDateTime.now());
            alert.setNotificationCount(alert.getNotificationCount() + 1);
        }
        alert.setUpdateTime(LocalDateTime.now());

        return alertRepository.save(alert);
    }

    @Override
    public Map<AlertSeverity, Long> countActiveAlertsBySeverity() {
        // 简单实现，实际可能需要根据具体需求优化
        Map<AlertSeverity, Long> result = new HashMap<>();
        List<AlertStatus> activeStatuses = Arrays.asList(AlertStatus.ACTIVE, AlertStatus.ACKNOWLEDGED);
        
        for (AlertSeverity severity : AlertSeverity.values()) {
            long count = alertRepository.countByStatusInAndSeverity(activeStatuses, severity);
            result.put(severity, count);
        }
        
        return result;
    }

    @Override
    public Map<String, Long> countActiveAlertsByAssetType() {
        List<AlertStatus> activeStatuses = Arrays.asList(AlertStatus.ACTIVE, AlertStatus.ACKNOWLEDGED);
        return alertRepository.countByStatusInGroupByAssetType(activeStatuses);
    }

    @Override
    public Map<String, Long> countAlertsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return alertRepository.countByDateGroupByDay(startDate, endDate);
    }

    @Override
    @Transactional
    public int processAlertEscalation() {
        // 告警升级逻辑，实际实现可能更复杂
        log.info("处理告警升级...");
        
        // 这里只是一个示例，实际可能需要根据不同级别告警的确认时间等条件进行升级
        List<Alert> alerts = alertRepository.findAlertsForEscalation();
        int count = 0;
        
        for (Alert alert : alerts) {
            // 根据业务逻辑进行升级处理
            // 例如：更新告警级别、触发特殊通知等
            count++;
        }
        
        return count;
    }
} 