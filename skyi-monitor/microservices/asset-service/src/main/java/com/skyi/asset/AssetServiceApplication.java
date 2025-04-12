package com.skyi.asset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 资产管理服务应用程序
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AssetServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AssetServiceApplication.class, args);
    }
} 