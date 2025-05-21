package com.certificate.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.certificate.common.constant.Constants;
import com.certificate.entity.Organization;
import com.certificate.service.CertificateService;
import com.certificate.service.LogService;
import com.certificate.service.OrganizationService;
import com.certificate.service.UserService;
import com.certificate.util.IpUtil;
import com.certificate.util.JwtUtil;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.admin.AdminDashboardVO;
import com.certificate.vo.org.OrgInfoVO;
import com.certificate.vo.org.OrgUpdateStatusVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")  // 保持原有路径
public class AdminController {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private LogService logService;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CertificateService certificateService;

    @Autowired
    private UserService userService;

    /**
     * 获取仪表盘数据
     */
    @GetMapping("/dashboard")
    public ResponseVO<AdminDashboardVO> getDashboardData() {
        // 保留原有实现
        System.out.println("AdminController - 请求仪表盘数据");

        try {
            // 统计机构数量
            int pendingCount = organizationService.countOrgByStatus(Constants.OrganizationStatus.PENDING);
            int approvedCount = organizationService.countOrgByStatus(Constants.OrganizationStatus.APPROVED);
            int rejectedCount = organizationService.countOrgByStatus(Constants.OrganizationStatus.REJECTED);
            int disabledCount = organizationService.countOrgByStatus(Constants.OrganizationStatus.DISABLED);

            // 获取最近注册的机构
            List<Organization> recentOrgs = organizationService.getRecentOrgs(5);

            // 构建仪表盘数据
            AdminDashboardVO dashboardVO = new AdminDashboardVO();
            dashboardVO.setPendingOrgCount(pendingCount);
            dashboardVO.setApprovedOrgCount(approvedCount);
            dashboardVO.setRejectedOrgCount(rejectedCount);
            dashboardVO.setDisabledOrgCount(disabledCount);
            dashboardVO.setRecentOrganizations(recentOrgs);

            // 添加其他统计数据
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("certTypeCount", 0);
            statistics.put("certCount", 0);
            dashboardVO.setStatistics(statistics);

            return ResponseVO.success("获取成功", dashboardVO);
        } catch (Exception e) {
            System.err.println("获取仪表盘数据出错: " + e.getMessage());
            e.printStackTrace();
            return ResponseVO.error("获取仪表盘数据失败");
        }
    }

    /**
     * 获取机构列表
     */
    @GetMapping("/organizations")
    public ResponseVO<IPage<Organization>> getOrgList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        // 保留原有实现
        System.out.println("AdminController - 获取机构列表: status=" + status + ", keyword=" + keyword);

        try {
            IPage<Organization> page = organizationService.getOrgList(status, keyword, pageNum, pageSize);
            return ResponseVO.success("获取成功", page);
        } catch (Exception e) {
            System.err.println("获取机构列表出错: " + e.getMessage());
            e.printStackTrace();
            return ResponseVO.error("获取机构列表失败");
        }
    }

    /**
     * 获取机构详情
     */
    @GetMapping("/organizations/{id}")
    public ResponseVO<OrgInfoVO> getOrgDetail(@PathVariable Long id) {
        System.out.println("AdminController - 获取机构详情: id=" + id);
        try {
            Organization org = organizationService.getById(id);
            if (org == null) {
                return ResponseVO.error("机构不存在");
            }

            // 组装VO
            OrgInfoVO vo = new OrgInfoVO();
            BeanUtils.copyProperties(org, vo);

            // 统计数据
            vo.setCertificates(certificateService.countByOrgId(id));
            vo.setUsers(userService.countByOrgId(id));
            vo.setTemplates(0); // 可删除前端模板卡片
            vo.setLastActive("从未");

            // 新增：最近发放的证书
            vo.setRecentCertificates(certificateService.getRecentCertificatesByOrgId(id, 5));

            return ResponseVO.success("获取成功", vo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("获取机构详情失败");
        }
    }

    /**
     * 更新机构状态
     */
    @PostMapping("/organizations/status")
    public ResponseVO<Boolean> updateOrgStatus(@Valid @RequestBody OrgUpdateStatusVO updateStatusVO, HttpServletRequest request) {
        System.out.println("AdminController - 更新机构状态: " + updateStatusVO);
        try {
            boolean result = organizationService.updateOrgStatus(updateStatusVO);

            // 添加操作日志
            String username = "admin"; // 如果不能从JWT中获取，可以直接使用默认值
            Long userId = null;
            try {
                username = jwtUtil.getUserIdFromRequest(request).toString();
                userId = jwtUtil.getUserIdFromRequest(request);
            } catch (Exception e) {
                // 忽略错误，使用默认值
            }

            String action = updateStatusVO.getStatus() == Constants.OrganizationStatus.APPROVED ? "审核通过" :
                    updateStatusVO.getStatus() == Constants.OrganizationStatus.REJECTED ? "审核拒绝" :
                            updateStatusVO.getStatus() == Constants.OrganizationStatus.DISABLED ? "禁用" : "启用";

            logService.addLog(
                    "operation",
                    "机构" + action,
                    username,
                    userId,
                    IpUtil.getIpAddress(request),
                    "success",
                    "管理员" + username + action + "了机构ID为" + updateStatusVO.getId() + "的申请" +
                            (updateStatusVO.getStatus() != Constants.OrganizationStatus.APPROVED ? "，原因：" + updateStatusVO.getRemark() : "")
            );

            return ResponseVO.success("更新成功", result);
        } catch (Exception e) {
            System.err.println("更新机构状态出错: " + e.getMessage());
            e.printStackTrace();
            return ResponseVO.error("更新机构状态失败");
        }
    }
}