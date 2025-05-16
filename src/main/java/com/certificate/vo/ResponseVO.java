package com.certificate.vo;

import lombok.Data;

@Data
public class ResponseVO<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> ResponseVO<T> success(String message) {
        return success(message, null);
    }

    public static <T> ResponseVO<T> success(String message, T data) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setCode(200);
        vo.setMessage(message);
        vo.setData(data);
        return vo;
    }

    public static <T> ResponseVO<T> error(String message) {
        return error(500, message);
    }

    public static <T> ResponseVO<T> error(Integer code, String message) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setCode(code);
        vo.setMessage(message);
        return vo;
    }

    public static <T> ResponseVO<T> error(String message, T data) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setCode(500);
        vo.setMessage(message);
        vo.setData(data);
        return vo;
    }
}