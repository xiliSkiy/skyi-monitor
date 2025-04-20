package com.skyi.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 存储服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
public class StorageServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(StorageServiceApplication.class, args);
    }
} 