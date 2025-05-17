// blockchain-certificate/src/main/java/com/certificate/vo/certificate/CertificateApplicationVO.java
package com.certificate.vo.certificate;

import lombok.Data;
import java.util.Date;
import java.util.Map;

@Data
public class CertificateApplicationVO {
    private Long id;
    private String applicationNo;
    private Long userId;
    private String userName;
    private Long orgId;
    private String orgName;
    private Long certificateTypeId;
    private String certificateTypeName;
    private Integer status;
    private String statusText;
    private String rejectReason;
    private Long certificateId;
    private Map<String, Object> applicationData;
    private Date applyTime;
    private Date reviewTime;
    private Long reviewerId;
    private String reviewerName;
}