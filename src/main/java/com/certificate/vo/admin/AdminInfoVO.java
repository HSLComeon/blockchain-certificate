package com.certificate.vo.admin;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class AdminInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String name;

    private String phone;

    private String email;

    private Integer status;

    private Date createTime;

    private String token;
}