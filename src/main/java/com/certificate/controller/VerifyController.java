package com.certificate.controller;

import com.certificate.service.CertificateService;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.certificate.CertificateVerifyResultVO;
import com.certificate.vo.certificate.CertificateVerifyVO;
import com.certificate.vo.certificate.CertificateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 证书验证控制器 - 公共API，不需要认证
 */
@RestController
@RequestMapping("/api/public/verify")  // 修改了路径前缀
public class VerifyController {

    @Autowired
    private CertificateService certificateService;

    /**
     * 验证证书
     */
    @PostMapping("/certificate")
    public ResponseVO<CertificateVerifyResultVO> verifyCertificate(@Valid @RequestBody CertificateVerifyVO verifyVO) {
        try {
            CertificateVerifyResultVO result = certificateService.verifyCertificate(verifyVO);
            if (result.isValid()) {
                return ResponseVO.success("验证通过", result);
            } else {
                return ResponseVO.error(result.getErrorMessage(), result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("验证失败: " + e.getMessage());
        }
    }

    /**
     * 根据证书编号查询证书
     */
    @GetMapping("/certificate/{certNo}")
    public ResponseVO<CertificateVO> getCertificate(@PathVariable String certNo) {
        try {
            CertificateVO certificateVO = certificateService.getCertificateByCertNo(certNo);
            return ResponseVO.success("获取成功", certificateVO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("获取证书失败: " + e.getMessage());
        }
    }
}