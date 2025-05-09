package com.certificate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "机构管理员注册参数")
public class InstitutionRegisterDTO {

    @ApiModelProperty(value = "用户名", required = true)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @ApiModelProperty(value = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;

    @ApiModelProperty(value = "机构名称", required = true)
    @NotBlank(message = "机构名称不能为空")
    private String institutionName;

    @ApiModelProperty(value = "机构描述")
    private String description;
}