package com.mk.common;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/18/17:53
 * @Description:
 */
@Data
public class Result<T> {
    /** 状态码：200成功，401未认证，403权限不足，500系统异常 */
    private Integer code;
    /** 提示信息 */
    private String msg;
    /** 数据（错误场景为null） */
    private T data;

    // 快速构建401结果
    public static <T> Result<T> unauthorized(String msg) {
        Result<T> result = new Result<>();
        result.setCode(401);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }

    // 快速构建403结果
    public static <T> Result<T> forbidden(String msg) {
        Result<T> result = new Result<>();
        result.setCode(403);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }
    
    // 快速构建错误结果
    public static <T> Result<T> error(int code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }
    
    // 快速构建错误结果（带数据）
    public static <T> Result<T> error(int code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
    
    // 快速构建成功结果
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("success");
        result.setData(data);
        return result;
    }
    
    // 快速构建成功结果（无数据）
    public static <T> Result<T> success() {
        return success(null);
    }
}
