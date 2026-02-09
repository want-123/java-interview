package com.mk.exception;

/**
 * 业务异常类
 * 用于在业务逻辑中抛出自定义的业务异常
 */
public class BusinessException extends RuntimeException {
    
    private int code;
    
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
}
