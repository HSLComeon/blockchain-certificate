package com.certificate.common.exception;

import com.certificate.common.api.ApiResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResult<Object> handleBusinessException(BusinessException e) {
        return ApiResult.failed(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<Object> handleException(Exception e) {
        return ApiResult.failed("系统异常，请联系管理员");
    }
}