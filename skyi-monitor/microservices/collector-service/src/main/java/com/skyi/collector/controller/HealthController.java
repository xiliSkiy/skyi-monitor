package com.skyi.collector.controller;

import com.skyi.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {
    
    /**
     * 健康检查接口
     *
     * @return 健康状态
     */
    @GetMapping
    public Result<Map<String, Object>> health() {
        log.info("健康检查请求");
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("service", "collector-service");
        healthInfo.put("timestamp", System.currentTimeMillis());
        return Result.success(healthInfo);
    }
} 