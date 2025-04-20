package com.skyi.alert.scheduler;

import com.skyi.alert.service.AlertService;
import com.skyi.alert.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 告警定时任务调度器
 */
@Slf4j
@Component
@EnableScheduling
public class AlertScheduler {

    @Autowired
    private AlertService alertService;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 处理告警升级，每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000)
    public void processAlertEscalation() {
        log.info("开始执行告警升级定时任务...");
        try {
            int count = alertService.processAlertEscalation();
            log.info("告警升级定时任务执行完成，处理告警数量: {}", count);
        } catch (Exception e) {
            log.error("告警升级定时任务执行异常", e);
        }
    }
    
    /**
     * 重试失败的告警通知，每2分钟执行一次
     */
    @Scheduled(fixedRate = 120000)
    public void retryFailedNotifications() {
        log.info("开始执行通知重试定时任务...");
        try {
            int count = notificationService.retryFailedNotifications();
            log.info("通知重试定时任务执行完成，重试通知数量: {}", count);
        } catch (Exception e) {
            log.error("通知重试定时任务执行异常", e);
        }
    }
} 