// blockchain-certificate/src/main/java/com/certificate/vo/org/OrgProfileUpdateVO.java
package com.certificate.vo.org;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class OrgProfileUpdateVO {
    @NotBlank(message = "机构名称不能为空")
    private String orgName;

    @NotBlank(message = "联系人不能为空")
    private String contactPerson;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;

    @NotBlank(message = "电子邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    private String address;

    private String description;
}