// blockchain-certificate/src/main/java/com/certificate/controller/OrgController.java
package com.certificate.controller;

import com.certificate.common.constant.Constants;
import com.certificate.entity.Organization;
import com.certificate.service.*;
import com.certificate.util.JwtUtil;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.org.OrgDashboardVO;
import com.certificate.vo.org.OrgInfoVO;
import com.certificate.vo.org.OrgProfileUpdateVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/org")
public class OrgController {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private CertificateApplicationService applicationService;

    @Autowired
    private CertificateRevocationService revocationService;

    @Autowired
    private UserService userService;

    /**
     * 获取机构信息
     * @param request HTTP请求
     * @return 机构信息
     */
    @GetMapping("/profile")
    public ResponseVO<OrgInfoVO> getOrgProfile(HttpServletRequest request) {
        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        Organization organization = organizationService.getById(orgId);
        if (organization == null) {
            return ResponseVO.error("机构不存在");
        }

        OrgInfoVO orgInfoVO = new OrgInfoVO();
        BeanUtils.copyProperties(organization, orgInfoVO);

        return ResponseVO.success("获取机构信息成功", orgInfoVO);
    }

    /**
     * 更新机构信息
     * @param profileUpdateVO 更新信息
     * @param request HTTP请求
     * @return 更新结果
     */
    @PutMapping("/profile")
    public ResponseVO<Boolean> updateOrgProfile(@RequestBody @Valid OrgProfileUpdateVO profileUpdateVO,
                                                HttpServletRequest request) {
        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        Organization organization = organizationService.getById(orgId);
        if (organization == null) {
            return ResponseVO.error("机构不存在");
        }

        // 更新机构信息
        organization.setOrgName(profileUpdateVO.getOrgName());
        organization.setContactPerson(profileUpdateVO.getContactPerson());
        organization.setContactPhone(profileUpdateVO.getContactPhone());
        organization.setEmail(profileUpdateVO.getEmail());
        organization.setAddress(profileUpdateVO.getAddress());
        organization.setDescription(profileUpdateVO.getDescription());

        boolean result = organizationService.updateById(organization);

        return result ? ResponseVO.success("更新机构信息成功", true) : ResponseVO.error("更新失败");
    }

    /**
     * 获取机构仪表盘数据
     * @param request HTTP请求
     * @return 仪表盘数据
     */
    @GetMapping("/dashboard")
    public ResponseVO<OrgDashboardVO> getOrgDashboard(HttpServletRequest request) {
        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        // 证书统计
        int totalCerts = certificateService.countByOrgId(orgId);
        int pendingCerts = certificateService.countByOrgIdAndStatus(orgId, Constants.CertificateStatus.PENDING);
        int issuedCerts = certificateService.countByOrgIdAndStatus(orgId, Constants.CertificateStatus.ON_CHAIN);
        int revokedCerts = certificateService.countByOrgIdAndStatus(orgId, Constants.CertificateStatus.REVOKED);

        // 申请统计
        int totalApps = applicationService.countByOrgId(orgId);
        int pendingApps = applicationService.countByOrgIdAndStatus(orgId, Constants.ApplicationStatus.PENDING);
        int approvedApps = applicationService.countByOrgIdAndStatus(orgId, Constants.ApplicationStatus.APPROVED);
        int rejectedApps = applicationService.countByOrgIdAndStatus(orgId, Constants.ApplicationStatus.REJECTED);

        // 注销统计
        int totalRevs = revocationService.countByOrgId(orgId);
        int pendingRevs = revocationService.countByOrgIdAndStatus(orgId, Constants.RevocationStatus.PENDING);
        int approvedRevs = revocationService.countByOrgIdAndStatus(orgId, Constants.RevocationStatus.APPROVED);
        int rejectedRevs = revocationService.countByOrgIdAndStatus(orgId, Constants.RevocationStatus.REJECTED);

        // 用户统计
        int totalUsers = userService.countByOrgId(orgId);
        int activeUsers = userService.countByOrgIdAndStatus(orgId, Constants.UserStatus.ENABLED);

        // 最近的申请和注销记录
        List<Map<String, Object>> recentApps = applicationService.getRecentOrgApplications(orgId, 5);
        List<Map<String, Object>> recentRevs = revocationService.getRecentOrgRevocations(orgId, 5);

        // 创建VO并返回
        OrgDashboardVO dashboardVO = new OrgDashboardVO();
        dashboardVO.setTotalCertificates(totalCerts);
        dashboardVO.setPendingCertificates(pendingCerts);
        dashboardVO.setIssuedCertificates(issuedCerts);
        dashboardVO.setRevokedCertificates(revokedCerts);

        dashboardVO.setTotalApplications(totalApps);
        dashboardVO.setPendingApplications(pendingApps);
        dashboardVO.setApprovedApplications(approvedApps);
        dashboardVO.setRejectedApplications(rejectedApps);

        dashboardVO.setTotalRevocations(totalRevs);
        dashboardVO.setPendingRevocations(pendingRevs);
        dashboardVO.setApprovedRevocations(approvedRevs);
        dashboardVO.setRejectedRevocations(rejectedRevs);

        dashboardVO.setTotalUsers(totalUsers);
        dashboardVO.setActiveUsers(activeUsers);

        dashboardVO.setRecentApplications(recentApps);
        dashboardVO.setRecentRevocations(recentRevs);

        return ResponseVO.success("获取仪表盘数据成功", dashboardVO);
    }
}