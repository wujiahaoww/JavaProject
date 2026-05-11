package com.travel.common.exception;

import com.travel.common.api.GlobalErrorCode;

/**
 * <p><b>作用：</b>表示「可预期的业务失败」（与程序 Bug 导致的 {@link RuntimeException} 区分）。</p>
 * <p>携带 {@link GlobalErrorCode} 中的业务码与建议的 HTTP 状态；
 * 全局异常处理器捕获后转为 {@link com.travel.common.api.ApiResult}，不把堆栈直接暴露给客户端。</p>
 */
public class BusinessException extends RuntimeException {

    /** 业务错误码，对应 {@link GlobalErrorCode#getCode()} */
    private final int code;

    /** HTTP 层状态码，对应 {@link GlobalErrorCode#getSuggestedHttpStatus()} 或业务需要 */
    private final int httpStatus;

    /**
     * 使用枚举中的默认提示文案作为异常消息。
     *
     * @param errorCode 业务错误枚举
     */
    public BusinessException(GlobalErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getSuggestedHttpStatus();
    }

    /**
     * 使用自定义详情覆盖默认 message（仍保留同一业务码与 HTTP 映射）。
     *
     * @param errorCode      业务错误枚举
     * @param detailMessage  展示给用户或日志的详细说明
     */
    public BusinessException(GlobalErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getSuggestedHttpStatus();
    }

    public int getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
