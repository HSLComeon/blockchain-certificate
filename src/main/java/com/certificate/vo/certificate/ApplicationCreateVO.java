// blockchain-certificate/src/main/java/com/certificate/vo/certificate/ApplicationCreateVO.java
package com.certificate.vo.certificate;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class ApplicationCreateVO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "证书类型ID不能为空")
    private Long certificateTypeId;

    // 申请数据，根据证书类型不同而不同
    private Map<String, Object> applicationData;
}