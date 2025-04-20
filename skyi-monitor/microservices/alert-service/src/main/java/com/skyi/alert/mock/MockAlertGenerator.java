package com.skyi.alert.mock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 模拟告警生成器
 * 用于开发和测试环境
 */
@Configuration
@EnableScheduling
@Profile({"dev-mock"})
@ConditionalOnProperty(name = "alert.mock-source.enabled", havingValue = "true")
@Slf4j
public class MockAlertGenerator {

    private final Random random = new Random();

    @Value("${alert.mock-source.interval-seconds:30}")
    private int intervalSeconds;

    private final List<String> alertNames = Arrays.asList(
            "CPU使用率过高",
            "内存使用率过高",
            "磁盘空间不足",
            "网络延迟异常",
            "服务响应超时",
            "数据库连接异常",
            "缓存命中率过低",
            "API调用频率异常",
            "队列堆积过多",
            "系统负载过高"
    );

    private final List<String> alertSeverities = Arrays.asList(
            "CRITICAL",
            "WARNING",
            "INFO"
    );

    private final List<String> resourceTypes = Arrays.asList(
            "HOST",
            "SERVICE",
            "DATABASE",
            "MIDDLEWARE",
            "APPLICATION"
    );

    private final List<String> resourceNames = Arrays.asList(
            "web-server-01",
            "app-server-02",
            "db-server-03",
            "cache-server-04",
            "api-service",
            "auth-service",
            "storage-service",
            "mysql-instance",
            "redis-instance",
            "kafka-broker"
    );

    /**
     * 定时生成模拟告警
     */
    @Scheduled(fixedRateString = "${alert.mock-source.interval-seconds:30}000")
    public void generateMockAlerts() {
        // 有一定几率生成告警
        if (random.nextInt(100) < 30) {
            String alertName = alertNames.get(random.nextInt(alertNames.size()));
            String severity = alertSeverities.get(random.nextInt(alertSeverities.size()));
            String resourceType = resourceTypes.get(random.nextInt(resourceTypes.size()));
            String resourceName = resourceNames.get(random.nextInt(resourceNames.size()));

            log.info("生成模拟告警: [{}] {} - {} on {} at {}", 
                    severity, alertName, resourceType, resourceName, LocalDateTime.now());

            // TODO: 实际项目中，这里应该调用告警服务的API或消息队列，发送告警
        }
    }
} 