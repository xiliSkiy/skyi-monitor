package com.skyi.processor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类
 */
@Slf4j
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    @Value("${processor.processing.thread-pool.core-size:5}")
    private int corePoolSize;
    
    @Value("${processor.processing.thread-pool.max-size:10}")
    private int maxPoolSize;
    
    @Value("${processor.processing.thread-pool.queue-capacity:500}")
    private int queueCapacity;
    
    @Value("${processor.processing.thread-pool.keep-alive:60}")
    private int keepAliveSeconds;
    
    /**
     * 数据处理线程池
     */
    @Bean("processorTaskExecutor")
    public ThreadPoolTaskExecutor processorTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("processor-");
        
        // 设置拒绝策略：由调用线程处理该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        log.info("初始化数据处理线程池: 核心线程数={}, 最大线程数={}, 队列容量={}", 
                corePoolSize, maxPoolSize, queueCapacity);
        
        return executor;
    }
    
    /**
     * 数据聚合线程池
     */
    @Bean("aggregationTaskExecutor")
    public ThreadPoolTaskExecutor aggregationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("aggregation-");
        
        // 设置拒绝策略：由调用线程处理该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        log.info("初始化数据聚合线程池: 核心线程数=2, 最大线程数=4, 队列容量=100");
        
        return executor;
    }
    
    /**
     * 存储操作线程池
     */
    @Bean("storageTaskExecutor")
    public ThreadPoolTaskExecutor storageTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("storage-");
        
        // 设置拒绝策略：由调用线程处理该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        log.info("初始化存储操作线程池: 核心线程数=3, 最大线程数=6, 队列容量=200");
        
        return executor;
    }
} 