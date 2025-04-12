package com.skyi.common.exception;

import lombok.Getter;

/**
 * 业务异常类
 */
@Getter
public class BusinessException extends RuntimeException {
    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 构造方法
     *
     * @param code 状态码
     * @param message 消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造方法（状态码默认500）
     *
     * @param message 消息
     */
    public BusinessException(String message) {
        this(500, message);
    }
} 