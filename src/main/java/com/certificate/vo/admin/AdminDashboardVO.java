package com.certificate.vo.admin;

import com.certificate.entity.Organization;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class AdminDashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 待审核机构数量
     */
    private Integer pendingOrgCount;

    /**
     * 已通过机构数量
     */
    private Integer approvedOrgCount;

    /**
     * 已拒绝机构数量
     */
    private Integer rejectedOrgCount;

    /**
     * 已禁用机构数量
     */
    private Integer disabledOrgCount;

    /**
     * 最近注册的机构
     */
    private List<Organization> recentOrganizations;

    /**
     * 系统统计数据（可扩展）
     */
    private Map<String, Object> statistics;
}