package com.certificate.controller;

import com.certificate.service.CertificateService;
import com.certificate.vo.ResponseVO;
import com.certificate.util.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Api(tags = "用户仪表盘接口")
public class UserDashboardController {

    @Autowired
    private CertificateService certificateService;

    @ApiOperation("获取用户仪表盘数据")
    @GetMapping("/dashboard")
    public ResponseVO<Map<String, Object>> getDashboard() {
        Long userId = SecurityUtils.getCurrentUserId();

        Map<String, Object> result = new HashMap<>();

        // 获取用户的证书总数
        int totalCertificates = certificateService.countByUserId(userId);
        // 获取用户已上链的证书数量
        int onChainCertificates = certificateService.countOnChainByUserId(userId);
        // 获取用户已撤销的证书数量
        int revokedCertificates = certificateService.countRevokedByUserId(userId);

        result.put("totalCertificates", totalCertificates);
        result.put("onChainCertificates", onChainCertificates);
        result.put("revokedCertificates", revokedCertificates);

        return ResponseVO.success("查询成功", result);
    }

    @ApiOperation("获取用户证书统计")
    @GetMapping("/certificate/stats")
    public ResponseVO<Map<String, Object>> getCertificateStats() {
        Long userId = SecurityUtils.getCurrentUserId();

        Map<String, Object> result = new HashMap<>();

        result.put("total", certificateService.countByUserId(userId));
        result.put("onChain", certificateService.countOnChainByUserId(userId));
        result.put("revoked", certificateService.countRevokedByUserId(userId));

        return ResponseVO.success("查询成功", result);
    }
}