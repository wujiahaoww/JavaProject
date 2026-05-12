package com.travel.config;

import com.travel.common.api.ApiResult;
import com.travel.common.api.GlobalErrorCode;
import com.travel.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p><b>作用：</b>全局捕获控制器层抛出的异常，统一转成 {@link ApiResult} JSON，并设置合适的 HTTP 状态码。</p>
 * <p>业务异常用明确错误码；未预期异常只返回通用文案并打日志，避免把堆栈或内部细节暴露给小程序 / 浏览器。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理 {@link BusinessException}：使用异常里携带的业务码、HTTP 状态与消息。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Void>> handleBusiness(BusinessException ex) {
        ApiResult<Void> body = new ApiResult<>();
        body.setCode(ex.getCode());
        body.setMessage(ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus()).body(body);
    }

    /**
     * 处理 {@code @Valid} 校验失败：取第一个字段错误，拼成可读说明，HTTP 400。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse(GlobalErrorCode.BAD_REQUEST.getMessage());
        return badRequest(GlobalErrorCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 请求体无法解析（如 JSON 格式错）或缺少必填 Query 参数：统一按「参数错误」返回 400。
     */
    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiResult<Void>> handleBadRequest(Exception ex) {
        return badRequest(GlobalErrorCode.BAD_REQUEST.getCode(), GlobalErrorCode.BAD_REQUEST.getMessage());
    }

    /**
     * 兜底：任何未单独处理的异常，记录完整栈，对外只返回「系统繁忙」类通用信息。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        ApiResult<Void> body = new ApiResult<>();
        body.setCode(GlobalErrorCode.INTERNAL_ERROR.getCode());
        body.setMessage(GlobalErrorCode.INTERNAL_ERROR.getMessage());
        return ResponseEntity.status(GlobalErrorCode.INTERNAL_ERROR.getSuggestedHttpStatus()).body(body);
    }

    /** 组装 HTTP 400 + 统一响应体的辅助方法 */
    private static ResponseEntity<ApiResult<Void>> badRequest(int code, String message) {
        ApiResult<Void> body = new ApiResult<>();
        body.setCode(code);
        body.setMessage(message);
        return ResponseEntity.badRequest().body(body);
    }
}
