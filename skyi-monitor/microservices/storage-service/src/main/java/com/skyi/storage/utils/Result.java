package com.skyi.storage.utils;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用响应结果
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 成功结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功结果
     */
    public static <T> Result<T> success(T data) {
        return success(data, "操作成功");
    }

    /**
     * 成功结果
     */
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setData(data);
        result.setMessage(message);
        result.setSuccess(true);
        return result;
    }

    /**
     * 失败结果
     */
    public static <T> Result<T> error() {
        return error("操作失败");
    }

    /**
     * 失败结果
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    /**
     * 失败结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }
} 