package com.skyi.collector.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * XXL-JOB任务调度配置
 */
@Slf4j
@Configuration
public class XxlJobConfig {
    
    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;
    
    @Value("${xxl.job.executor.appname}")
    private String appName;
    
    @Value("${xxl.job.executor.ip}")
    private String ip;
    
    @Value("${xxl.job.executor.port}")
    private int port;
    
    @Value("${xxl.job.executor.logpath}")
    private String logPath;
    
    @Value("${xxl.job.executor.logretentiondays}")
    private int logRetentionDays;
    
    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(appName);
        xxlJobSpringExecutor.setIp(ip);
        xxlJobSpringExecutor.setPort(port);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);
        
        return xxlJobSpringExecutor;
    }
} 