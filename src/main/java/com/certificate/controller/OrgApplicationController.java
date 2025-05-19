// blockchain-certificate/src/main/java/com/certificate/controller/OrgApplicationController.java
package com.certificate.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.certificate.common.constant.Constants;
import com.certificate.entity.CertificateApplication;
import com.certificate.entity.User;
import com.certificate.service.CertificateApplicationService;
import com.certificate.service.CertificateTypeService;
import com.certificate.service.UserService;
import com.certificate.util.JwtUtil;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.certificate.ApplicationCreateVO;
import com.certificate.vo.certificate.CertificateApplicationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/org/applications")
public class OrgApplicationController {

    @Autowired
    private CertificateApplicationService applicationService;

    @Autowired
    private CertificateTypeService certificateTypeService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取机构证书申请列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param status 状态
     * @param keyword 关键字
     * @param request HTTP请求
     * @return 申请列表
     */
    @GetMapping
    public ResponseVO<IPage<CertificateApplicationVO>> getOrgApplications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {

        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        // 查询申请列表
        IPage<CertificateApplicationVO> applicationPage = applicationService.getOrgApplicationList(
                orgId, status, keyword, pageNum, pageSize);

        return ResponseVO.success("获取申请列表成功", applicationPage);
    }

    /**
     * 创建证书申请
     * @param createVO 创建信息
     * @param request HTTP请求
     * @return 创建结果
     */
    @PostMapping
    public ResponseVO<Boolean> createApplication(
            @RequestBody @Valid ApplicationCreateVO createVO,
            HttpServletRequest request) {

        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        // 验证用户是否存在且属于当前机构
        User user = userService.getById(createVO.getUserId());
        if (user == null) {
            return ResponseVO.error("用户不存在");
        }

        if (!user.getOrgId().equals(orgId)) {
            return ResponseVO.error("无权为其他机构的用户申请证书");
        }

        // 验证证书类型是否存在
        if (certificateTypeService.getById(createVO.getCertificateTypeId()) == null) {
            return ResponseVO.error("证书类型不存在");
        }

        // 创建申请实体
        CertificateApplication application = new CertificateApplication();
        application.setUserId(createVO.getUserId());
        application.setOrgId(orgId);
        application.setCertificateTypeId(createVO.getCertificateTypeId());
        application.setApplicationData(JSON.toJSONString(createVO.getApplicationData()));

        try {
            boolean result = applicationService.createApplication(application);
            return result ?
                    ResponseVO.success("创建申请成功", true) :
                    ResponseVO.error("创建申请失败");
        } catch (Exception e) {
            return ResponseVO.error(e.getMessage());
        }
    }

    /**
     * 获取申请详情
     * @param id 申请ID
     * @param request HTTP请求
     * @return 申请详情
     */
    @GetMapping("/{id}")
    public ResponseVO<CertificateApplicationVO> getApplicationDetail(
            @PathVariable Long id,
            HttpServletRequest request) {

        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        // 获取申请
        CertificateApplication application = applicationService.getById(id);
        if (application == null) {
            return ResponseVO.error("申请不存在");
        }

        // 验证申请是否属于当前机构
        if (!application.getOrgId().equals(orgId)) {
            return ResponseVO.error("无权查看此申请");
        }

        // 获取申请详情
        IPage<CertificateApplicationVO> page = applicationService.getApplicationList(
                null, "id:" + id, 1, 1);

        if (page.getRecords().isEmpty()) {
            return ResponseVO.error("获取申请详情失败");
        }

        return ResponseVO.success("获取申请详情成功", page.getRecords().get(0));
    }

    /**
     * 取消证书申请
     * @param id 申请ID
     * @param request HTTP请求
     * @return 取消结果
     */
    @PostMapping("/{id}/cancel")
    public ResponseVO<Boolean> cancelApplication(
            @PathVariable Long id,
            HttpServletRequest request) {

        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        // 获取申请
        CertificateApplication application = applicationService.getById(id);
        if (application == null) {
            return ResponseVO.error("申请不存在");
        }

        // 验证申请是否属于当前机构
        if (!application.getOrgId().equals(orgId)) {
            return ResponseVO.error("无权取消此申请");
        }

        try {
            boolean result = applicationService.cancelApplication(id, application.getUserId());
            return result ?
                    ResponseVO.success("取消申请成功", true) :
                    ResponseVO.error("取消申请失败");
        } catch (Exception e) {
            return ResponseVO.error(e.getMessage());
        }
    }
}