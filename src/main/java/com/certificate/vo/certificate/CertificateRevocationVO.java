// blockchain-certificate/src/main/java/com/certificate/vo/certificate/CertificateRevocationVO.java
package com.certificate.vo.certificate;

import lombok.Data;
import java.util.Date;

@Data
public class CertificateRevocationVO {
    private Long id;
    private String revocationNo;
    private Long certificateId;
    private Long userId;
    private String userName;
    private String reason;
    private Integer status;
    private String statusText;
    private String rejectReason;
    private String txHash;
    private Date applyTime;
    private Date reviewTime;
    private Long reviewerId;
    private String reviewerName;
    private Date revocationTime;
}