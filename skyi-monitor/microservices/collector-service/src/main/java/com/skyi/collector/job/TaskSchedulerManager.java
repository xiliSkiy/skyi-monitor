package com.skyi.collector.job;

import com.cronutils.mapper.CronMapper;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.skyi.collector.model.CollectorTask;
import com.skyi.collector.model.CollectorTaskSchedule;
import com.skyi.collector.repository.CollectorTaskRepository;
import com.skyi.collector.repository.CollectorTaskScheduleRepository;
import com.skyi.collector.service.CollectorTaskExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 任务调度管理器
 * 负责动态执行界面配置的调度任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskSchedulerManager {
    
    private final CollectorTaskRepository taskRepository;
    private final CollectorTaskScheduleRepository scheduleRepository;
    private final CollectorTaskExecutor taskExecutor;
    
    // Cron表达式解析器
    private final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
    private final CronParser parser = new CronParser(cronDefinition);
    
    /**
     * 执行固定频率的调度任务
     * 每10秒检查一次
     */
    @Scheduled(fixedRate = 10000)
    public void executeFixedRateSchedules() {
        LocalDateTime now = LocalDateTime.now();
        log.debug("检查固定频率调度任务: {}", now);
        
        try {
            // 查询需要执行的固定频率调度
            List<CollectorTaskSchedule> schedules = scheduleRepository.findFixedRateSchedulesToExecute(now);
            log.info("发现{}个固定频率调度需要执行", schedules.size());
            
            for (CollectorTaskSchedule schedule : schedules) {
                try {
                    // 获取对应的任务
                    CollectorTask task = taskRepository.findById(schedule.getTaskId()).orElse(null);
                    if (task == null) {
                        log.warn("任务不存在: taskId={}", schedule.getTaskId());
                        continue;
                    }
                    
                    // 执行任务
                    taskExecutor.scheduleTask(task, false);
                    
                    // 更新调度记录
                    schedule.setLastExecuteTime(now);
                    // 计算下次执行时间
                    schedule.setNextExecuteTime(now.plusSeconds(schedule.getFixedRate()));
                    scheduleRepository.save(schedule);
                    
                } catch (Exception e) {
                    log.error("执行固定频率调度出错: scheduleId={}, {}", schedule.getId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("执行固定频率调度过程出错: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 执行一次性调度任务
     * 每1分钟检查一次
     */
    @Scheduled(fixedRate = 60000)
    public void executeOneTimeSchedules() {
        LocalDateTime now = LocalDateTime.now();
        log.debug("检查一次性调度任务: {}", now);
        
        try {
            // 查询需要执行的一次性调度
            List<CollectorTaskSchedule> schedules = scheduleRepository.findOneTimeSchedulesToExecute(now);
            log.info("发现{}个一次性调度需要执行", schedules.size());
            
            for (CollectorTaskSchedule schedule : schedules) {
                try {
                    // 获取对应的任务
                    CollectorTask task = taskRepository.findById(schedule.getTaskId()).orElse(null);
                    if (task == null) {
                        log.warn("任务不存在: taskId={}", schedule.getTaskId());
                        continue;
                    }
                    
                    // 执行任务
                    taskExecutor.scheduleTask(task, false);
                    
                    // 更新调度记录
                    schedule.setLastExecuteTime(now);
                    schedule.setEnabled(0); // 执行完成后自动禁用
                    scheduleRepository.save(schedule);
                    
                } catch (Exception e) {
                    log.error("执行一次性调度出错: scheduleId={}, {}", schedule.getId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("执行一次性调度过程出错: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 执行CRON调度任务
     * 每1分钟检查一次
     */
    @Scheduled(fixedRate = 60000)
    public void executeCronSchedules() {
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime zonedNow = now.atZone(ZoneId.systemDefault());
        log.debug("检查CRON调度任务: {}", now);
        
        try {
            // 查询当前有效的CRON调度
            List<CollectorTaskSchedule> schedules = scheduleRepository.findValidCronSchedules(now);
            log.info("发现{}个CRON调度需要检查", schedules.size());
            
            for (CollectorTaskSchedule schedule : schedules) {
                try {
                    // 解析CRON表达式
                    Cron cron = parser.parse(schedule.getCronExpression());
                    ExecutionTime executionTime = ExecutionTime.forCron(cron);
                    
                    // 检查是否需要执行
                    LocalDateTime lastExecuteTime = schedule.getLastExecuteTime();
                    if (lastExecuteTime == null) {
                        // 从未执行过，检查当前时间是否应该执行
                        Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(zonedNow.minusMinutes(1));
                        if (nextExecution.isPresent() && !nextExecution.get().isAfter(zonedNow)) {
                            executeSchedule(schedule, now);
                        }
                    } else {
                        // 已执行过，检查上次执行时间到现在是否应该执行
                        ZonedDateTime lastZoned = lastExecuteTime.atZone(ZoneId.systemDefault());
                        Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(lastZoned);
                        if (nextExecution.isPresent() && !nextExecution.get().isAfter(zonedNow)) {
                            executeSchedule(schedule, now);
                        }
                    }
                    
                } catch (Exception e) {
                    log.error("检查CRON调度出错: scheduleId={}, {}", schedule.getId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("执行CRON调度过程出错: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 执行调度
     *
     * @param schedule 调度配置
     * @param now 当前时间
     */
    private void executeSchedule(CollectorTaskSchedule schedule, LocalDateTime now) {
        try {
            // 获取对应的任务
            CollectorTask task = taskRepository.findById(schedule.getTaskId()).orElse(null);
            if (task == null) {
                log.warn("任务不存在: taskId={}", schedule.getTaskId());
                return;
            }
            
            // 执行任务
            taskExecutor.scheduleTask(task, false);
            
            // 更新调度记录
            schedule.setLastExecuteTime(now);
            
            // 计算下次执行时间（仅更新显示用，实际调度不依赖此字段）
            if (schedule.getScheduleType() == 2) { // CRON
                try {
                    Cron cron = parser.parse(schedule.getCronExpression());
                    ExecutionTime executionTime = ExecutionTime.forCron(cron);
                    ZonedDateTime zonedNow = now.atZone(ZoneId.systemDefault());
                    Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(zonedNow);
                    if (nextExecution.isPresent()) {
                        schedule.setNextExecuteTime(nextExecution.get().toLocalDateTime());
                    }
                } catch (Exception e) {
                    log.warn("计算CRON下次执行时间出错: {}", e.getMessage());
                }
            }
            
            scheduleRepository.save(schedule);
            
        } catch (Exception e) {
            log.error("执行调度任务出错: scheduleId={}, {}", schedule.getId(), e.getMessage(), e);
        }
    }
} 