package com.skyi.alert.constant;

/**
 * Kafka主题常量
 */
public class KafkaTopicConstants {

    /**
     * 阈值超限告警主题
     */
    public static final String TOPIC_THRESHOLD_ALERT = "threshold-alert";
    
    /**
     * 趋势告警主题
     */
    public static final String TOPIC_TREND_ALERT = "trend-alert";
    
    /**
     * 异常模式告警主题
     */
    public static final String TOPIC_PATTERN_ALERT = "pattern-alert";
    
    /**
     * 告警状态更新主题
     */
    public static final String TOPIC_ALERT_STATUS_UPDATE = "alert-status-update";
    
    /**
     * 告警通知主题
     */
    public static final String TOPIC_ALERT_NOTIFICATION = "alert-notification";
    
    /**
     * 告警升级主题
     */
    public static final String TOPIC_ALERT_ESCALATION = "alert-escalation";
    
    /**
     * WebSocket实时推送主题
     */
    public static final String TOPIC_WEBSOCKET_PUSH = "websocket-push";
} 