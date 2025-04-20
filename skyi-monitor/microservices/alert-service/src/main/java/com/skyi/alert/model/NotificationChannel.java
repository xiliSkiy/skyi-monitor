package com.skyi.alert.model;

/**
 * 通知渠道枚举
 */
public enum NotificationChannel {
    /**
     * 邮件
     */
    EMAIL,
    
    /**
     * 短信
     */
    SMS,
    
    /**
     * Webhook回调
     */
    WEBHOOK,
    
    /**
     * 钉钉
     */
    DINGTALK,
    
    /**
     * 企业微信
     */
    WEIXIN
} 