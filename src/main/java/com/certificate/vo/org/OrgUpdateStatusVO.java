package com.certificate.vo.org;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class OrgUpdateStatusVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 机构ID
     */
    @NotNull(message = "机构ID不能为空")
    private Long id;

    /**
     * 状态（0：待审核，1：已通过，2：已拒绝, 3: 已禁用）
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 审核意见
     */
    private String remark;
}