package com.hnit.server.dto;

/**
 * @ClassName Result
 * @Description TODO
 * @Author IFundo
 * @Date 00:01 2025/9/23
 * @Version 1.0
 */
public class Result<T> {

    private int code;

    private String message;

    private T data;

    public Result(ResultCode resultCode) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public Result(ResultCode resultCode, T data) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS, data);
    }

    public static <T> Result<T> failed() {
        return new Result<>(ResultCode.FAILED);
    }

    public static <T> Result<T> failed(String customMessage) {
        Result<T> result = new Result<>(ResultCode.FAILED);
        result.setMessage(customMessage);
        return result;
    }

}
