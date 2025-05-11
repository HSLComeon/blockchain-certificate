package com.certificate.vo.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class UserRegisterVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String name;

    private String idCard;

    private String phone;

    private String email;

    private String gender;

    private String captcha;

    private String captchaKey;
}