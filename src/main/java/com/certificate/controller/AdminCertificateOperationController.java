// blockchain-certificate/src/main/java/com/certificate/controller/AdminCertificateOperationController.java
package com.certificate.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.certificate.service.CertificateApplicationService;
import com.certificate.service.CertificateRevocationService;
import com.certificate.util.JwtUtil;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.certificate.CertificateApplicationVO;
import com.certificate.vo.certificate.CertificateRevocationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/certificate-operations")
public class AdminCertificateOperationController {

    @Autowired
    private CertificateApplicationService applicationService;
    @Autowired
    private CertificateRevocationService revocationService;
    @Autowired
    private JwtUtil jwtUtil;

    // 证书申请列表
    @GetMapping("/applications")
    public ResponseVO<IPage<CertificateApplicationVO>> getApplications(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<CertificateApplicationVO> page = applicationService.getApplicationList(status, keyword, pageNum, pageSize);
        return ResponseVO.success("获取成功", page);
    }

    // 审核证书申请
    @PostMapping("/applications/review")
    public ResponseVO<Boolean> reviewApplication(
            @RequestParam Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String rejectReason,
            HttpServletRequest request) {
        Long reviewerId = jwtUtil.getUserIdFromRequest(request);
        boolean result = applicationService.reviewApplication(id, status, rejectReason, reviewerId);
        return ResponseVO.success("审核成功", result);
    }

    // 注销申请列表
    @GetMapping("/revocations")
    public ResponseVO<IPage<CertificateRevocationVO>> getRevocations(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<CertificateRevocationVO> page = revocationService.getRevocationList(status, keyword, pageNum, pageSize);
        return ResponseVO.success("获取成功", page);
    }

    // 审核注销申请
    @PostMapping("/revocations/review")
    public ResponseVO<Boolean> reviewRevocation(
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        String rejectReason = params.get("rejectReason") != null ? params.get("rejectReason").toString() : null;
        Long reviewerId = jwtUtil.getUserIdFromRequest(request);
        boolean result = revocationService.reviewRevocation(id, status, rejectReason, reviewerId);
        return ResponseVO.success("审核成功", result);
    }

    // 注销详情
    @GetMapping("/revocations/{id}")
    public ResponseVO<CertificateRevocationVO> getRevocationDetail(@PathVariable Long id) {
        CertificateRevocationVO vo = revocationService.getRevocationDetailVO(id);
        if (vo == null) {
            return ResponseVO.error("注销申请不存在");
        }
        return ResponseVO.success("获取注销申请详情成功", vo);
    }
}