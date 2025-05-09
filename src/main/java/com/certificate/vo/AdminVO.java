package com.certificate.vo;

import lombok.Data;

@Data
public class AdminVO {
    private Long id;
    private String username;
    private Integer role;
    private Long institutionId;
    private String institutionName;
    private String token;
}