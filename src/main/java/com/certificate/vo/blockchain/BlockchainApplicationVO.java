// blockchain-certificate/src/main/java/com/certificate/vo/blockchain/BlockchainApplicationVO.java
package com.certificate.vo.blockchain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlockchainApplicationVO {

    private Long id;

    private String applicationNo;

    private Long certificateId;

    private String certificateNo;

    private String certificateTitle;

    private Long orgId;

    private String orgName;

    private Integer status;

    private String statusName;

    private String reason;

    private String rejectReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime;

    private Long reviewerId;

    private String reviewerName;

    private String txHash;
}