// blockchain-certificate/src/main/java/com/certificate/vo/user/OrgUserUpdateVO.java
package com.certificate.vo.user;

import lombok.Data;

import javax.validation.constraints.Pattern;

@Data
public class OrgUserUpdateVO {

    private String name;

    @Pattern(regexp = "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)", message = "身份证号格式不正确")
    private String idCard;

    private String phone;

    private String email;

    private String gender;

    private String password;
}