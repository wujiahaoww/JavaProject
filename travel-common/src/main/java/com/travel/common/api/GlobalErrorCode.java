package com.travel.common.api;

/**
 * 全局错误码枚举（占位）。
 */
public enum GlobalErrorCode {

    SUCCESS(0, "OK"),
    BAD_REQUEST(40000, "参数错误"),
    UNAUTHORIZED(40100, "未登录或令牌无效"),
    FORBIDDEN(40300, "无权限"),
    NOT_FOUND(40400, "资源不存在"),
    TOO_MANY_REQUESTS(42900, "请求过于频繁"),
    INTERNAL_ERROR(50000, "系统繁忙，请稍后重试");

    private final int code;
    private final String message;

    GlobalErrorCode(int code, String message) {
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
