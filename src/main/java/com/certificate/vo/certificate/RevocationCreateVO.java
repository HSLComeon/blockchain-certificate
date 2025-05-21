// blockchain-certificate/src/main/java/com/certificate/vo/certificate/RevocationCreateVO.java

package com.certificate.vo.certificate;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RevocationCreateVO {

    @NotNull(message = "证书ID不能为空")
    private Long certificateId;

    // 移除userId字段，使用证书中的userId

    @NotBlank(message = "注销原因不能为空")
    private String reason;
}