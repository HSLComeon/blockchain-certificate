package com.certificate.vo.certificate;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CertificateVerifyVO {

    /**
     * 证书编号
     */
    @NotBlank(message = "证书编号不能为空")
    private String certNo;

    /**
     * 证书哈希
     */
    @NotBlank(message = "证书哈希不能为空")
    private String hash;
}