package com.skyi.alert.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Web客户端配置类
 */
@Configuration
public class WebClientConfig {

    /**
     * 配置RestTemplate Bean
     * @return RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 