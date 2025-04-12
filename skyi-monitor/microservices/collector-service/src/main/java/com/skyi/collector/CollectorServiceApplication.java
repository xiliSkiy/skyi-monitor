package com.skyi.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 数据采集服务应用程序
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class CollectorServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CollectorServiceApplication.class, args);
    }
} 