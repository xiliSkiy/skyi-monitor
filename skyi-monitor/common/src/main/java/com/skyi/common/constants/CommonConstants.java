package com.skyi.common.constants;

/**
 * 公共常量类
 */
public class CommonConstants {
    /**
     * 成功状态码
     */
    public static final Integer SUCCESS_CODE = 200;
    
    /**
     * 失败状态码
     */
    public static final Integer ERROR_CODE = 500;
    
    /**
     * 未授权状态码
     */
    public static final Integer UNAUTHORIZED_CODE = 401;
    
    /**
     * 禁止访问状态码
     */
    public static final Integer FORBIDDEN_CODE = 403;
    
    /**
     * 资产类型 - 服务器
     */
    public static final String ASSET_TYPE_SERVER = "server";
    
    /**
     * 资产类型 - 数据库
     */
    public static final String ASSET_TYPE_DATABASE = "database";
    
    /**
     * 资产类型 - 中间件
     */
    public static final String ASSET_TYPE_MIDDLEWARE = "middleware";
    
    /**
     * 资产类型 - 应用
     */
    public static final String ASSET_TYPE_APPLICATION = "application";
    
    /**
     * 告警级别 - 严重
     */
    public static final String ALERT_LEVEL_CRITICAL = "critical";
    
    /**
     * 告警级别 - 警告
     */
    public static final String ALERT_LEVEL_WARNING = "warning";
    
    /**
     * 告警级别 - 提示
     */
    public static final String ALERT_LEVEL_INFO = "info";
    
    /**
     * 告警状态 - 未处理
     */
    public static final String ALERT_STATUS_UNPROCESSED = "unprocessed";
    
    /**
     * 告警状态 - 已确认
     */
    public static final String ALERT_STATUS_CONFIRMED = "confirmed";
    
    /**
     * 告警状态 - 已解决
     */
    public static final String ALERT_STATUS_RESOLVED = "resolved";
} 