package com.travel.common.exception;

import com.travel.common.api.GlobalErrorCode;

/**
 * 业务异常（占位）。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(GlobalErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(GlobalErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
