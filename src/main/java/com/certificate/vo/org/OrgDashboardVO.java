// blockchain-certificate/src/main/java/com/certificate/vo/org/OrgDashboardVO.java
package com.certificate.vo.org;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OrgDashboardVO {
    // 证书统计
    private Integer totalCertificates;
    private Integer pendingCertificates;
    private Integer issuedCertificates;
    private Integer revokedCertificates;

    // 申请统计
    private Integer totalApplications;
    private Integer pendingApplications;
    private Integer approvedApplications;
    private Integer rejectedApplications;

    // 注销统计
    private Integer totalRevocations;
    private Integer pendingRevocations;
    private Integer approvedRevocations;
    private Integer rejectedRevocations;

    // 用户统计
    private Integer totalUsers;
    private Integer activeUsers;

    // 最近的证书申请
    private List<Map<String, Object>> recentApplications;

    // 最近的证书注销
    private List<Map<String, Object>> recentRevocations;
}