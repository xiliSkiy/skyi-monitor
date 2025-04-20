package com.skyi.processor.service.task;

import com.skyi.processor.service.MetricProcessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 指标聚合定时任务
 * 负责定期执行指标数据的聚合操作
 */
@Slf4j
@Component
public class MetricAggregationTask {

    @Autowired
    private MetricProcessorService metricProcessorService;
    
    @Value("${processor.processing.aggregation.enabled:true}")
    private boolean aggregationEnabled;
    
    /**
     * 系统指标列表
     */
    private static final List<String> SYSTEM_METRICS = Arrays.asList(
            "cpu_usage", "memory_usage", "disk_usage", "network_traffic");
    
    /**
     * 每小时执行一次1分钟聚合
     */
    @Scheduled(cron = "${processor.processing.aggregation.schedule.1m:0 0 * * * ?}")
    public void aggregateOneMinute() {
        if (!aggregationEnabled) {
            return;
        }
        
        log.info("开始执行1分钟指标聚合任务");
        executeAggregation(1);
        log.info("完成1分钟指标聚合任务");
    }
    
    /**
     * 每天凌晨1点执行一次5分钟聚合
     */
    @Scheduled(cron = "${processor.processing.aggregation.schedule.5m:0 0 1 * * ?}")
    public void aggregateFiveMinutes() {
        if (!aggregationEnabled) {
            return;
        }
        
        log.info("开始执行5分钟指标聚合任务");
        executeAggregation(5);
        log.info("完成5分钟指标聚合任务");
    }
    
    /**
     * 每周一凌晨2点执行一次1小时聚合
     */
    @Scheduled(cron = "${processor.processing.aggregation.schedule.1h:0 0 2 ? * MON}")
    public void aggregateOneHour() {
        if (!aggregationEnabled) {
            return;
        }
        
        log.info("开始执行1小时指标聚合任务");
        executeAggregation(60);
        log.info("完成1小时指标聚合任务");
    }
    
    /**
     * 执行指定时间间隔的聚合操作
     *
     * @param intervalMinutes 时间间隔（分钟）
     */
    private void executeAggregation(int intervalMinutes) {
        try {
            // 为每个系统指标执行聚合
            for (String metricName : SYSTEM_METRICS) {
                try {
                    metricProcessorService.aggregateMetrics(metricName, intervalMinutes);
                    log.debug("成功聚合指标 {}, 间隔: {}分钟", metricName, intervalMinutes);
                } catch (Exception e) {
                    log.error("聚合指标 {} 失败, 间隔: {}分钟", metricName, intervalMinutes, e);
                }
            }
        } catch (Exception e) {
            log.error("执行聚合任务异常", e);
        }
    }
} 