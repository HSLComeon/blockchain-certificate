package com.certificate.vo.certificate;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Map;

@Data
public class CertificateCreateVO {

    /**
     * 证书类型ID
     */
    @NotNull(message = "证书类型不能为空")
    private Long typeId;

    /**
     * 持有人姓名
     */
    @NotBlank(message = "持有人姓名不能为空")
    private String holderName;

    /**
     * 持有人身份证号
     */
    @NotBlank(message = "持有人身份证号不能为空")
    private String holderIdCard;

    /**
     * 证书标题
     */
    @NotBlank(message = "证书标题不能为空")
    private String title;

    /**
     * 证书内容（JSON格式，包含证书属性和值）
     */
    @NotNull(message = "证书内容不能为空")
    private Map<String, Object> content;

    /**
     * 发证日期
     */
    @NotNull(message = "发证日期不能为空")
    private Date issueDate;

    /**
     * 有效期开始日期
     */
    @NotNull(message = "有效期开始日期不能为空")
    private Date validFromDate;

    /**
     * 有效期结束日期（null表示永久有效）
     */
    private Date validToDate;

    /**
     * 用户ID（可选，如果已有用户则关联）
     */
    private Long userId;
}