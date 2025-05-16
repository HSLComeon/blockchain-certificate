package com.certificate.vo.certificateType;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

@Data
public class CertificateTypeCreateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 证书类型名称
     */
    @NotBlank(message = "证书类型名称不能为空")
    private String name;

    /**
     * 证书类型描述
     */
    @NotBlank(message = "证书类型描述不能为空")
    private String description;

    /**
     * 标识颜色
     */
    @NotBlank(message = "标识颜色不能为空")
    private String color;

    /**
     * 证书属性列表
     */
    @NotEmpty(message = "证书属性不能为空")
    private List<String> attributes;
}