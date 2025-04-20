package com.skyi.collector.service.collector;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 连接测试结果类
 * 包含连接测试的结果状态和相关信息
 */
@Data
@Builder
public class ConnectionTestResult {
    /**
     * 测试是否成功
     */
    private boolean success;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 错误详情
     */
    private String errorDetail;
    
    /**
     * 额外信息
     */
    @Builder.Default
    private Map<String, Object> details = new HashMap<>();
    
    /**
     * 连接延迟时间(毫秒)
     */
    private long latency;
    
    /**
     * 创建一个成功的连接测试结果
     * 
     * @param message 消息
     * @param latency 延迟(毫秒)
     * @return 连接测试结果
     */
    public static ConnectionTestResult success(String message, long latency) {
        return ConnectionTestResult.builder()
                .success(true)
                .message(message)
                .latency(latency)
                .build();
    }
    
    /**
     * 创建一个失败的连接测试结果
     * 
     * @param message 消息
     * @param errorDetail 错误详情
     * @return 连接测试结果
     */
    public static ConnectionTestResult failure(String message, String errorDetail) {
        return ConnectionTestResult.builder()
                .success(false)
                .message(message)
                .errorDetail(errorDetail)
                .build();
    }
    
    /**
     * 添加额外信息
     * 
     * @param key 键
     * @param value 值
     * @return 当前对象
     */
    public ConnectionTestResult addDetail(String key, Object value) {
        if (details == null) {
            details = new HashMap<>();
        }
        details.put(key, value);
        return this;
    }
} 