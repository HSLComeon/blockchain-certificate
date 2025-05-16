package com.certificate.vo.certificate;

import com.certificate.entity.Certificate;
import com.certificate.entity.CertificateType;
import com.certificate.entity.Organization;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class CertificateVO {

    private Long id;

    private String certNo;

    private Long typeId;

    private String typeName;

    private Long orgId;

    private String orgName;

    private Long userId;

    private String holderName;

    private String holderIdCard;

    private String title;

    private Map<String, Object> content;

    private Date issueDate;

    private Date validFromDate;

    private Date validToDate;

    private String blockchainTxHash;

    private Integer status;

    private String statusText;

    private String hash;

    private Date createTime;

    private Date updateTime;

    // 证书类型信息
    private CertificateType type;

    // 机构信息
    private Organization organization;
}