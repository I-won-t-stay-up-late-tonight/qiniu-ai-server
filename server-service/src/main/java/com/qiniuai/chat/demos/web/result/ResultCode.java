package com.qiniuai.chat.demos.web.result;

public enum ResultCode {
    SUCCESS(200, "success"),
    FAILED(301, "failed");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
