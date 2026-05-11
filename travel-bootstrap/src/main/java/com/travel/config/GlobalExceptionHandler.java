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
 * 全局异常 → 统一 {@link ApiResult} + HTTP 状态，避免栈信息直接暴露给客户端（非业务异常仅返回通用文案）。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Void>> handleBusiness(BusinessException ex) {
        ApiResult<Void> body = new ApiResult<>();
        body.setCode(ex.getCode());
        body.setMessage(ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse(GlobalErrorCode.BAD_REQUEST.getMessage());
        return badRequest(GlobalErrorCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiResult<Void>> handleBadRequest(Exception ex) {
        return badRequest(GlobalErrorCode.BAD_REQUEST.getCode(), GlobalErrorCode.BAD_REQUEST.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        ApiResult<Void> body = new ApiResult<>();
        body.setCode(GlobalErrorCode.INTERNAL_ERROR.getCode());
        body.setMessage(GlobalErrorCode.INTERNAL_ERROR.getMessage());
        return ResponseEntity.status(GlobalErrorCode.INTERNAL_ERROR.getSuggestedHttpStatus()).body(body);
    }

    private static ResponseEntity<ApiResult<Void>> badRequest(int code, String message) {
        ApiResult<Void> body = new ApiResult<>();
        body.setCode(code);
        body.setMessage(message);
        return ResponseEntity.badRequest().body(body);
    }
}
