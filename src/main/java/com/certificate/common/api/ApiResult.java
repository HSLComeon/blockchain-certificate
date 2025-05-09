package com.certificate.common.api;

import lombok.Data;

@Data
public class ApiResult<T> {
    private int code;
    private String message;
    private T data;

    private ApiResult() {}

    public static <T> ApiResult<T> success() {
        return success(null);
    }

    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> ApiResult<T> failed(String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(ResultCode.FAILED.getCode());
        result.setMessage(message);
        return result;
    }

    public static <T> ApiResult<T> failed(ResultCode resultCode) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        return result;
    }
}