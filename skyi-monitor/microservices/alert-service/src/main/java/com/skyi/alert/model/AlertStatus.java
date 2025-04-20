package com.skyi.alert.model;

/**
 * 告警状态枚举
 */
public enum AlertStatus {
    /**
     * 未处理
     */
    ACTIVE,
    
    /**
     * 已确认
     */
    ACKNOWLEDGED,
    
    /**
     * 已解决
     */
    RESOLVED,
    
    /**
     * 已关闭
     */
    CLOSED
} 