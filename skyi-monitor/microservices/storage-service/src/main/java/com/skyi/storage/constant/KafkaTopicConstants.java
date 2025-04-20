package com.skyi.storage.constant;

/**
 * Kafka主题常量
 */
public class KafkaTopicConstants {

    /**
     * 时序数据写入主题
     */
    public static final String TOPIC_TIMESERIES_DATA = "timeseries-data";
    
    /**
     * 时序数据批量写入主题
     */
    public static final String TOPIC_TIMESERIES_BATCH = "timeseries-batch";
    
    /**
     * 元数据写入主题
     */
    public static final String TOPIC_METADATA_SAVE = "metadata-save";
    
    /**
     * 元数据更新主题
     */
    public static final String TOPIC_METADATA_UPDATE = "metadata-update";
    
    /**
     * 元数据删除主题
     */
    public static final String TOPIC_METADATA_DELETE = "metadata-delete";
    
    /**
     * 缓存更新主题
     */
    public static final String TOPIC_CACHE_UPDATE = "cache-update";
    
    /**
     * 缓存失效主题
     */
    public static final String TOPIC_CACHE_INVALIDATE = "cache-invalidate";
} 