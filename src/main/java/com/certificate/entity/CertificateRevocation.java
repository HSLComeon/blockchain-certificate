// blockchain-certificate/src/main/java/com/certificate/entity/CertificateRevocation.java
package com.certificate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("certificate_revocation")
public class CertificateRevocation implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String revocationNo;
    private Long certificateId;
    private Long userId;
    private String reason;
    private Integer status; // 0-待审核，1-已批准，2-已拒绝
    private String rejectReason;
    private String txHash;
    private Date applyTime;
    private Date reviewTime;
    private Long reviewerId;
    private Date revocationTime;
}