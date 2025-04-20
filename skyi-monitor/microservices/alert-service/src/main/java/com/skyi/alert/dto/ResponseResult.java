package com.skyi.alert.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseResult<T> {
    /**
     * 状态码
     */
    private Integer code;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 数据
     */
    private T data;
    
    /**
     * 成功响应
     */
    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>(200, "操作成功", data);
    }
    
    /**
     * 成功响应（自定义消息）
     */
    public static <T> ResponseResult<T> success(String message, T data) {
        return new ResponseResult<>(200, message, data);
    }
    
    /**
     * 失败响应
     */
    public static <T> ResponseResult<T> error(String message) {
        return new ResponseResult<>(500, message, null);
    }
    
    /**
     * 失败响应（自定义错误码）
     */
    public static <T> ResponseResult<T> error(Integer code, String message) {
        return new ResponseResult<>(code, message, null);
    }
    
    /**
     * 未授权响应
     */
    public static <T> ResponseResult<T> unauthorized(String message) {
        return new ResponseResult<>(401, message, null);
    }
    
    /**
     * 禁止访问响应
     */
    public static <T> ResponseResult<T> forbidden(String message) {
        return new ResponseResult<>(403, message, null);
    }
    
    /**
     * 资源不存在响应
     */
    public static <T> ResponseResult<T> notFound(String message) {
        return new ResponseResult<>(404, message, null);
    }
} 