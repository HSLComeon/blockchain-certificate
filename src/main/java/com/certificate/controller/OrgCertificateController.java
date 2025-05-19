// blockchain-certificate/src/main/java/com/certificate/controller/OrgCertificateController.java
package com.certificate.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.certificate.entity.CertificateBlockchainApplication;
import com.certificate.service.CertificateService;
import com.certificate.util.JwtUtil;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.blockchain.BlockchainApplicationCreateVO;
import com.certificate.vo.certificate.CertificateCreateVO;
import com.certificate.vo.certificate.CertificateVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;


@Slf4j  // 添加此注解
@RestController
@RequestMapping("/org/certificates")
public class OrgCertificateController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取机构证书列表
     */
    @GetMapping
    public ResponseVO<IPage<CertificateVO>> getCertificates(
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

        // 调用服务获取证书列表
        IPage<CertificateVO> page = certificateService.getCertificateList(
                orgId, status, keyword, pageNum, pageSize);

        return ResponseVO.success("获取成功", page);
    }

    /**
     * 创建证书
     */
    @PostMapping
    public ResponseVO<CertificateVO> createCertificate(
            @RequestBody @Valid CertificateCreateVO createVO,
            HttpServletRequest request) {

        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        // 创建证书
        CertificateVO certificateVO = certificateService.createCertificate(createVO, orgId);

        return ResponseVO.success("创建成功", certificateVO);
    }

    /**
     * 获取证书详情
     */
    @GetMapping("/{id}")
    public ResponseVO<CertificateVO> getCertificateDetail(
            @PathVariable Long id,
            HttpServletRequest request) {

        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        // 获取证书详情
        CertificateVO certificateVO = certificateService.getCertificateDetail(id);

        // 验证权限
        if (!certificateVO.getOrgId().equals(orgId)) {
            return ResponseVO.error("无权查看此证书");
        }

        return ResponseVO.success("获取成功", certificateVO);
    }

    /**
     * 上链证书
     */
    @PostMapping("/{id}/upload")
    public ResponseVO<CertificateVO> uploadToBlockchain(
            @PathVariable Long id,
            HttpServletRequest request) {

        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        // 获取证书详情
        CertificateVO certificateVO = certificateService.getCertificateDetail(id);

        // 验证权限
        if (!certificateVO.getOrgId().equals(orgId)) {
            return ResponseVO.error("无权操作此证书");
        }

        // 上链
        CertificateVO result = certificateService.uploadToBlockchain(id);

        return ResponseVO.success("上链成功", result);
    }

    /**
     * 批量上链
     */
    @PostMapping("/batch-upload")
    public ResponseVO<Integer> batchUpload(
            @RequestBody List<Long> ids,
            HttpServletRequest request) {

        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        // 验证每个证书的权限
        for (Long id : ids) {
            CertificateVO certificateVO = certificateService.getCertificateDetail(id);
            if (!certificateVO.getOrgId().equals(orgId)) {
                return ResponseVO.error("无权操作ID为" + id + "的证书");
            }
        }

        // 批量上链
        int count = certificateService.batchUploadToBlockchain(ids);

        return ResponseVO.success("成功上链" + count + "个证书", count);
    }

    /**
     * 生成证书二维码
     */
    @GetMapping("/{id}/qrcode")
    public ResponseVO<String> generateQRCode(
            @PathVariable Long id,
            HttpServletRequest request) {

        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        // 获取证书详情
        CertificateVO certificateVO = certificateService.getCertificateDetail(id);

        // 验证权限
        if (!certificateVO.getOrgId().equals(orgId)) {
            return ResponseVO.error("无权操作此证书");
        }

        // 生成二维码
        String qrCodeBase64 = certificateService.generateQRCode(id);

        return ResponseVO.success("生成成功", qrCodeBase64);
    }
}