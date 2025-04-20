package com.skyi.alert.repository;

import com.skyi.alert.model.Alert;
import com.skyi.alert.model.AlertSeverity;
import com.skyi.alert.model.AlertStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 告警仓库接口
 */
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /**
     * 根据告警UUID查找告警
     */
    Alert findByAlertUuid(String alertUuid);
    
    /**
     * 根据资产ID和状态查找告警
     */
    List<Alert> findByAssetIdAndStatus(Long assetId, AlertStatus status);
    
    /**
     * 根据资产ID、指标名称和状态查找告警
     */
    Alert findByAssetIdAndMetricNameAndStatus(Long assetId, String metricName, AlertStatus status);
    
    /**
     * 分页查询告警
     */
    Page<Alert> findByStatusIn(List<AlertStatus> statuses, Pageable pageable);
    
    /**
     * 根据资产ID分页查询
     */
    Page<Alert> findByAssetId(Long assetId, Pageable pageable);
    
    /**
     * 分页查询某个资产的告警
     */
    Page<Alert> findByAssetIdAndStatusIn(Long assetId, List<AlertStatus> statuses, Pageable pageable);
    
    /**
     * 按告警级别统计未处理和已确认的告警数量
     */
    @Query("SELECT a.severity, COUNT(a) FROM Alert a WHERE a.status IN :statuses GROUP BY a.severity")
    List<Object[]> countAlertsBySeverity(@Param("statuses") List<AlertStatus> statuses);
    
    /**
     * 统计指定状态和级别的告警数量
     */
    long countByStatusInAndSeverity(List<AlertStatus> statuses, AlertSeverity severity);
    
    /**
     * 查询需要发送通知的告警
     */
    List<Alert> findByStatusInAndNotifiedFalse(List<AlertStatus> statuses);
    
    /**
     * 查询需要发送通知的告警（包含时间阈值）
     */
    @Query("SELECT a FROM Alert a WHERE a.status IN :statuses AND (a.notified = false OR a.lastNotifiedTime < :timeThreshold)")
    List<Alert> findAlertsForNotification(@Param("statuses") List<AlertStatus> statuses, @Param("timeThreshold") LocalDateTime timeThreshold);
    
    /**
     * 查询指定级别的未处理告警
     */
    List<Alert> findByStatusAndSeverity(AlertStatus status, AlertSeverity severity);
    
    /**
     * 按资产类型统计告警
     */
    @Query("SELECT a.assetType, COUNT(a) FROM Alert a WHERE a.status IN :statuses GROUP BY a.assetType")
    List<Object[]> countAlertsByAssetType(@Param("statuses") List<AlertStatus> statuses);
    
    /**
     * 按资产类型统计告警（返回Map形式）
     */
    @Query("SELECT a.assetType as type, COUNT(a) as count FROM Alert a WHERE a.status IN :statuses GROUP BY a.assetType")
    Map<String, Long> countByStatusInGroupByAssetType(@Param("statuses") List<AlertStatus> statuses);
    
    /**
     * 按时间范围统计告警数量
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', a.createTime, '%Y-%m-%d') as date, COUNT(a) as count FROM Alert a WHERE a.createTime BETWEEN :startDate AND :endDate GROUP BY FUNCTION('DATE_FORMAT', a.createTime, '%Y-%m-%d')")
    Map<String, Long> countByDateGroupByDay(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * 查询需要升级的告警
     */
    @Query("SELECT a FROM Alert a WHERE a.status = 'ACTIVE' AND a.createTime < :timeThreshold AND a.notificationCount < :maxNotifications")
    List<Alert> findAlertsForEscalation(@Param("timeThreshold") LocalDateTime timeThreshold, @Param("maxNotifications") int maxNotifications);
    
    /**
     * 查询需要升级的告警（简化版本，实际业务逻辑可能更复杂）
     */
    @Query("SELECT a FROM Alert a WHERE a.status = 'ACTIVE' AND a.notificationCount < 3")
    List<Alert> findAlertsForEscalation();
} 