package com.certificate.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseVO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer code;

    private String message;

    private T data;

    public static <T> ResponseVO<T> success(T data) {
        return new ResponseVO<>(200, "操作成功", data);
    }

    public static <T> ResponseVO<T> success(String message, T data) {
        return new ResponseVO<>(200, message, data);
    }

    public static <T> ResponseVO<T> error(String message) {
        return new ResponseVO<>(500, message, null);
    }

    public static <T> ResponseVO<T> error(Integer code, String message) {
        return new ResponseVO<>(code, message, null);
    }
}