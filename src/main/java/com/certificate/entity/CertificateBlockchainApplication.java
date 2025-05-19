// src/main/java/com/certificate/entity/CertificateBlockchainApplication.java
package com.certificate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("certificate_blockchain_application")
public class CertificateBlockchainApplication {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String applicationNo;

    private Long certificateId;

    private Long orgId;

    private Integer status;

    private String reason;

    private String rejectReason;

    private LocalDateTime applyTime;

    private LocalDateTime reviewTime;

    private Long reviewerId;

    private String txHash;
}