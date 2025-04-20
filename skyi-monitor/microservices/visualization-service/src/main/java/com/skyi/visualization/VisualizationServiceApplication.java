package com.skyi.visualization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 可视化服务主应用程序
 */
@EnableFeignClients
@EnableScheduling
@SpringBootApplication
public class VisualizationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisualizationServiceApplication.class, args);
    }
} 