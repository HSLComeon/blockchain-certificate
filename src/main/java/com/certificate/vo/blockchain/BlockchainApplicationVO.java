// src/main/java/com/certificate/vo/blockchain/BlockchainApplicationVO.java
package com.certificate.vo.blockchain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlockchainApplicationVO {

    private Long id;

    private String applicationNo;

    private Long certificateId;

    private String certNo;

    private String certTitle;

    private String holderName;

    private Long orgId;

    private String orgName;

    private Integer status;

    private String reason;

    private String rejectReason;

    private LocalDateTime applyTime;

    private LocalDateTime reviewTime;

    private Long reviewerId;

    private String reviewerName;

    private String txHash;
}