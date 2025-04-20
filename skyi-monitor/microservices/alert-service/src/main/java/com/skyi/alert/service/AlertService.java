package com.skyi.alert.service;

import com.skyi.alert.dto.AlertDTO;
import com.skyi.alert.dto.ThresholdAlertDTO;
import com.skyi.alert.model.Alert;
import com.skyi.alert.model.AlertSeverity;
import com.skyi.alert.model.AlertStatus;
import com.skyi.alert.model.AlertNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 告警服务接口
 */
public interface AlertService {

    /**
     * 处理阈值告警
     *
     * @param thresholdAlertDTO 阈值告警数据
     * @return 生成的告警
     */
    Alert processThresholdAlert(ThresholdAlertDTO thresholdAlertDTO);
    
    /**
     * 根据ID查询告警
     *
     * @param id 告警ID
     * @return 告警对象
     */
    Alert getAlertById(Long id);
    
    /**
     * 根据UUID查询告警
     *
     * @param uuid 告警UUID
     * @return 告警对象
     */
    Alert getAlertByUuid(String uuid);
    
    /**
     * 获取告警列表
     * @param statuses 告警状态列表，如果为空则返回所有状态
     * @param pageable 分页参数
     * @return 分页后的告警列表
     */
    Page<Alert> getAlerts(List<AlertStatus> statuses, Pageable pageable);
    
    /**
     * 根据资产ID获取告警列表
     * @param assetId 资产ID
     * @param statuses 告警状态列表，如果为空则返回所有状态
     * @param pageable 分页参数
     * @return 分页后的告警列表
     */
    Page<Alert> getAlertsByAssetId(Long assetId, List<AlertStatus> statuses, Pageable pageable);
    
    /**
     * 创建新告警
     * @param alert 告警对象
     * @return 创建后的告警对象
     */
    Alert createAlert(Alert alert);
    
    /**
     * 确认告警
     * @param id 告警ID
     * @param acknowledgedBy 确认人
     * @return 更新后的告警对象
     */
    Alert acknowledgeAlert(Long id, String acknowledgedBy);
    
    /**
     * 解决告警
     * @param id 告警ID
     * @param resolvedBy 解决人
     * @param comment 解决说明
     * @return 更新后的告警对象
     */
    Alert resolveAlert(Long id, String resolvedBy, String comment);
    
    /**
     * 关闭告警
     *
     * @param alertId 告警ID
     * @return 是否成功
     */
    boolean closeAlert(Long alertId);
    
    /**
     * 查询需要发送通知的告警
     *
     * @return 需要通知的告警列表
     */
    List<Alert> findAlertsForNotification();
    
    /**
     * 更新告警通知状态
     *
     * @param alertId 告警ID
     * @param notified 是否已通知
     * @return 更新后的告警
     */
    Alert updateAlertNotificationStatus(Long alertId, boolean notified);
    
    /**
     * 按严重程度统计活跃告警数量
     * @return 以告警严重程度为键，数量为值的映射
     */
    Map<AlertSeverity, Long> countActiveAlertsBySeverity();
    
    /**
     * 按资产类型统计活跃告警数量
     * @return 以资产类型为键，数量为值的映射
     */
    Map<String, Long> countActiveAlertsByAssetType();
    
    /**
     * 按日期范围统计告警数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 以日期字符串为键，数量为值的映射
     */
    Map<String, Long> countAlertsByDateRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 批量处理告警升级
     *
     * @return 处理的告警数量
     */
    int processAlertEscalation();
    
    /**
     * 处理告警通知
     * @param alertId 告警ID
     */
    void processAlertNotification(Long alertId);

    /**
     * 获取告警的通知记录
     * @param alertId 告警ID
     * @return 通知记录列表
     */
    List<AlertNotification> getAlertNotifications(Long alertId);
} 