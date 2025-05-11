package com.certificate.vo.org;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class OrgRegisterVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "机构名称不能为空")
    private String orgName;

    private String orgCode;

    private String address;

    private String contactPerson;

    private String contactPhone;

    private String email;

    private String description;

    private String captcha;

    private String captchaKey;
}