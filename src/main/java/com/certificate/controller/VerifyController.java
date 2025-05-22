package com.certificate.controller;

import com.certificate.service.CertificateService;
import com.certificate.service.LogService;
import com.certificate.util.IpUtil;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.certificate.CertificateVerifyResultVO;
import com.certificate.vo.certificate.CertificateVerifyVO;
import com.certificate.vo.certificate.CertificateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/verify")
public class VerifyController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private LogService logService;

    /**
     * 验证证书
     */
    @PostMapping
    public ResponseVO<CertificateVerifyResultVO> verifyCertificate(
            @Valid @RequestBody CertificateVerifyVO verifyVO,
            HttpServletRequest request) {
        try {
            CertificateVerifyResultVO resultVO = certificateService.verifyCertificate(verifyVO);

            // 记录验证日志
            logService.addLog(
                    "verify",
                    "证书验证",
                    "anonymous",
                    null,
                    IpUtil.getIpAddress(request),
                    resultVO.isValid() ? "success" : "fail",
                    "验证证书：" + verifyVO.getCertNo() + "，结果：" + (resultVO.isValid() ? "有效" : "无效")
            );

            return ResponseVO.success("验证完成", resultVO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("验证失败: " + e.getMessage());
        }
    }

    /**
     * 根据证书编号查询证书
     */
    @GetMapping("/{certNo}")
    public ResponseVO<CertificateVO> getCertificateByCertNo(
            @PathVariable String certNo,
            HttpServletRequest request) {
        try {
            CertificateVO certificateVO = certificateService.getCertificateByCertNo(certNo);

            if (certificateVO == null) {
                return ResponseVO.error("证书不存在");
            }

            // 记录查询日志
            logService.addLog(
                    "query",
                    "证书查询",
                    "anonymous",
                    null,
                    IpUtil.getIpAddress(request),
                    "success",
                    "查询证书：" + certNo
            );

            return ResponseVO.success("查询成功", certificateVO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("查询失败: " + e.getMessage());
        }
    }
}