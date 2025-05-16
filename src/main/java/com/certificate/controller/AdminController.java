package com.certificate.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.certificate.common.constant.Constants;
import com.certificate.entity.Organization;
import com.certificate.service.OrganizationService;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.admin.AdminDashboardVO;
import com.certificate.vo.org.OrgUpdateStatusVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private OrganizationService organizationService;

    /**
     * 获取仪表盘数据
     */
    @GetMapping("/dashboard")
    public ResponseVO<AdminDashboardVO> getDashboardData() {
        // 添加调试日志
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
            statistics.put("certTypeCount", 0); // 暂时设为0，后续从证书类型服务获取
            statistics.put("certCount", 0);     // 暂时设为0，后续从证书服务获取
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

        // 添加调试日志
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
    public ResponseVO<Organization> getOrgDetail(@PathVariable Long id) {
        System.out.println("AdminController - 获取机构详情: id=" + id);
        try {
            Organization org = organizationService.getById(id);
            if (org == null) {
                return ResponseVO.error("机构不存在");
            }
            return ResponseVO.success("获取成功", org);
        } catch (Exception e) {
            System.err.println("获取机构详情出错: " + e.getMessage());
            e.printStackTrace();
            return ResponseVO.error("获取机构详情失败");
        }
    }

    /**
     * 更新机构状态
     */
    @PostMapping("/organizations/status")
    public ResponseVO<Boolean> updateOrgStatus(@Valid @RequestBody OrgUpdateStatusVO updateStatusVO) {
        System.out.println("AdminController - 更新机构状态: " + updateStatusVO);
        try {
            boolean result = organizationService.updateOrgStatus(updateStatusVO);
            return ResponseVO.success("更新成功", result);
        } catch (Exception e) {
            System.err.println("更新机构状态出错: " + e.getMessage());
            e.printStackTrace();
            return ResponseVO.error("更新机构状态失败");
        }
    }
}