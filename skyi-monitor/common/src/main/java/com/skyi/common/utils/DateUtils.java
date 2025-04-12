package com.skyi.common.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 日期工具类
 */
public class DateUtils {
    /**
     * 默认日期时间格式
     */
    private static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 默认日期格式
     */
    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    
    /**
     * 默认时间格式
     */
    private static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

    /**
     * 获取当前时间的字符串表示，格式为 yyyy-MM-dd HH:mm:ss
     *
     * @return 日期时间字符串
     */
    public static String getNowString() {
        return format(LocalDateTime.now(), DEFAULT_DATETIME_PATTERN);
    }

    /**
     * 获取当前时间的字符串表示，使用指定格式
     *
     * @param pattern 日期格式
     * @return 日期时间字符串
     */
    public static String getNowString(String pattern) {
        return format(LocalDateTime.now(), pattern);
    }

    /**
     * 将 LocalDateTime 格式化为字符串，默认格式为 yyyy-MM-dd HH:mm:ss
     *
     * @param dateTime 日期时间
     * @return 日期时间字符串
     */
    public static String format(LocalDateTime dateTime) {
        return format(dateTime, DEFAULT_DATETIME_PATTERN);
    }

    /**
     * 将 LocalDateTime 格式化为字符串，使用指定格式
     *
     * @param dateTime 日期时间
     * @param pattern 日期格式
     * @return 日期时间字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }

    /**
     * 将毫秒时间戳转换为 LocalDateTime
     *
     * @param timestamp 毫秒时间戳
     * @return LocalDateTime
     */
    public static LocalDateTime timestampToDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    /**
     * 将 LocalDateTime 转换为毫秒时间戳
     *
     * @param dateTime LocalDateTime
     * @return 毫秒时间戳
     */
    public static long dateTimeToTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
} 