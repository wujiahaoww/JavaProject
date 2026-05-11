package com.travel.config;

import com.travel.common.api.ApiResult;
import com.travel.common.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理（占位：可补充校验异常、404 等映射）。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Void>> handleBusiness(BusinessException ex) {
        ApiResult<Void> body = new ApiResult<>();
        body.setCode(ex.getCode());
        body.setMessage(ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }
}
