package com.certificate.vo.certificate;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CertificateRevokeVO {

    /**
     * 证书ID
     */
    @NotNull(message = "证书ID不能为空")
    private Long id;

    /**
     * 撤销原因
     */
    private String reason;
}