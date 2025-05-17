// blockchain-certificate/src/main/java/com/certificate/entity/CertificateApplication.java
package com.certificate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("certificate_application")
public class CertificateApplication implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String applicationNo;
    private Long userId;
    private Long orgId;
    private Long certificateTypeId;
    private Integer status; // 0-待审核，1-已批准，2-已拒绝
    private String rejectReason;
    private Long certificateId;
    private String applicationData;
    private Date applyTime;
    private Date reviewTime;
    private Long reviewerId;
}