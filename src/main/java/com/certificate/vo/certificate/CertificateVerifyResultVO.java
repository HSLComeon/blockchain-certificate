package com.certificate.vo.certificate;

import lombok.Data;

@Data
public class CertificateVerifyResultVO {

    /**
     * 验证结果：true-通过，false-不通过
     */
    private boolean valid;

    /**
     * 证书信息（验证通过时返回）
     */
    private CertificateVO certificate;

    /**
     * 错误消息（验证不通过时返回）
     */
    private String errorMessage;

    public static CertificateVerifyResultVO success(CertificateVO certificate) {
        CertificateVerifyResultVO result = new CertificateVerifyResultVO();
        result.setValid(true);
        result.setCertificate(certificate);
        return result;
    }

    public static CertificateVerifyResultVO failure(String errorMessage) {
        CertificateVerifyResultVO result = new CertificateVerifyResultVO();
        result.setValid(false);
        result.setErrorMessage(errorMessage);
        return result;
    }
}