package com.hnit.server.dto;

import lombok.Data;

/**
 * API统一响应模型
 */
@Data
public class ApiResult<T> {
    private int code;
    private String message;
    private T data;

    private ApiResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应
     */
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(200, "success", data);
    }

    /**
     * 失败响应
     */
    public static <T> ApiResult<T> fail(String message) {
        return new ApiResult<>(500, message, null);
    }

    /**
     * 失败响应
     */
    public static <T> ApiResult<T> fail(int code, String message) {
        return new ApiResult<>(code, message, null);
    }
}
