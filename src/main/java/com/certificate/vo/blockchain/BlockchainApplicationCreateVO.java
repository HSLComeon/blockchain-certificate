// src/main/java/com/certificate/vo/blockchain/BlockchainApplicationCreateVO.java
package com.certificate.vo.blockchain;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class BlockchainApplicationCreateVO {

    @NotNull(message = "证书ID不能为空")
    private Long certificateId;

    private String reason;
}