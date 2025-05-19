// blockchain-certificate/src/main/java/com/certificate/controller/OrgRevocationController.java
package com.certificate.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.certificate.entity.Certificate;
import com.certificate.entity.CertificateRevocation;
import com.certificate.entity.User;
import com.certificate.service.CertificateRevocationService;
import com.certificate.service.CertificateService;
import com.certificate.service.UserService;
import com.certificate.util.JwtUtil;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.certificate.CertificateRevocationVO;
import com.certificate.vo.certificate.RevocationCreateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/org/revocations")
public class OrgRevocationController {

    @Autowired
    private CertificateRevocationService revocationService;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取机构证书注销申请列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param status 状态
     * @param keyword 关键字
     * @param request HTTP请求
     * @return 注销申请列表
     */
    @GetMapping
    public ResponseVO<IPage<CertificateRevocationVO>> getOrgRevocations(
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

        // 查询注销申请列表
        IPage<CertificateRevocationVO> revocationPage = revocationService.getOrgRevocationList(
                orgId, status, keyword, pageNum, pageSize);

        return ResponseVO.success("获取注销申请列表成功", revocationPage);
    }

    /**
     * 创建证书注销申请
     * @param createVO 创建信息
     * @param request HTTP请求
     * @return 创建结果
     */
    @PostMapping
    public ResponseVO<Boolean> createRevocation(
            @RequestBody @Valid RevocationCreateVO createVO,
            HttpServletRequest request) {

        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        // 验证证书是否存在且属于当前机构
        Certificate certificate = certificateService.getById(createVO.getCertificateId());
        if (certificate == null) {
            return ResponseVO.error("证书不存在");
        }

        if (!certificate.getOrgId().equals(orgId)) {
            return ResponseVO.error("无权为其他机构的证书申请注销");
        }

        // 验证用户是否存在且属于当前机构
        User user = userService.getById(createVO.getUserId());
        if (user == null) {
            return ResponseVO.error("用户不存在");
        }

        if (!user.getOrgId().equals(orgId)) {
            return ResponseVO.error("无权为其他机构的用户申请注销");
        }

        // 创建注销申请实体
        CertificateRevocation revocation = new CertificateRevocation();
        revocation.setCertificateId(createVO.getCertificateId());
        revocation.setUserId(createVO.getUserId());
        revocation.setReason(createVO.getReason());

        try {
            boolean result = revocationService.createRevocation(revocation);
            return result ?
                    ResponseVO.success("创建注销申请成功", true) :
                    ResponseVO.error("创建注销申请失败");
        } catch (Exception e) {
            return ResponseVO.error(e.getMessage());
        }
    }

    /**
     * 获取注销申请详情
     * @param id 注销申请ID
     * @param request HTTP请求
     * @return 注销申请详情
     */
    @GetMapping("/{id}")
    public ResponseVO<CertificateRevocationVO> getRevocationDetail(
            @PathVariable Long id,
            HttpServletRequest request) {

        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        // 获取注销申请
        CertificateRevocation revocation = revocationService.getById(id);
        if (revocation == null) {
            return ResponseVO.error("注销申请不存在");
        }

        // 验证注销申请是否属于当前机构
        Certificate certificate = certificateService.getById(revocation.getCertificateId());
        if (certificate == null || !certificate.getOrgId().equals(orgId)) {
            return ResponseVO.error("无权查看此注销申请");
        }

        // 获取注销申请详情
        IPage<CertificateRevocationVO> page = revocationService.getRevocationList(
                null, "id:" + id, 1, 1);

        if (page.getRecords().isEmpty()) {
            return ResponseVO.error("获取注销申请详情失败");
        }

        return ResponseVO.success("获取注销申请详情成功", page.getRecords().get(0));
    }

    /**
     * 取消证书注销申请
     * @param id 注销申请ID
     * @param request HTTP请求
     * @return 取消结果
     */
    @PostMapping("/{id}/cancel")
    public ResponseVO<Boolean> cancelRevocation(
            @PathVariable Long id,
            HttpServletRequest request) {

        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        // 获取注销申请
        CertificateRevocation revocation = revocationService.getById(id);
        if (revocation == null) {
            return ResponseVO.error("注销申请不存在");
        }

        // 验证注销申请是否属于当前机构
        Certificate certificate = certificateService.getById(revocation.getCertificateId());
        if (certificate == null || !certificate.getOrgId().equals(orgId)) {
            return ResponseVO.error("无权取消此注销申请");
        }

        try {
            boolean result = revocationService.cancelRevocation(id, revocation.getUserId());
            return result ?
                    ResponseVO.success("取消注销申请成功", true) :
                    ResponseVO.error("取消注销申请失败");
        } catch (Exception e) {
            return ResponseVO.error(e.getMessage());
        }
    }
}