package com.certificate.vo.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String name;

    private String idCard;

    private String phone;

    private String email;

    private String gender;

    private Integer status;

    private Date createTime;

    private String token;
}