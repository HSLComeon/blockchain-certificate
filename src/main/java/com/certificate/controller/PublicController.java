package com.certificate.controller;

import com.certificate.entity.Certificate;
import com.certificate.entity.Organization;
import com.certificate.entity.User;
import com.certificate.service.CertificateService;
import com.certificate.service.OrganizationService;
import com.certificate.service.UserService;
import com.certificate.vo.ResponseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/public")
@Api(tags = "公共接口")
public class PublicController {

    @Autowired
    private CertificateService certificateService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private UserService userService;

    @ApiOperation("验证证书真伪")
    @GetMapping("/verify/certificate/{certificateNo}")
    public ResponseVO<Map<String, Object>> verifyCertificate(@PathVariable String certificateNo) {
        Certificate certificate = certificateService.getByCertificateNo(certificateNo);

        if (certificate == null) {
            return ResponseVO.error("证书不存在");
        }

        Map<String, Object> result = new HashMap<>();

        // 基本信息
        result.put("id", certificate.getId());
        result.put("name", certificate.getName());
        result.put("certificateNo", certificate.getCertificateNo());
        result.put("issueDate", certificate.getIssueDate());
        result.put("expireDate", certificate.getExpireDate());
        result.put("validFromDate", certificate.getValidFromDate());
        result.put("status", certificate.getStatus());

        // 组织信息
        Organization org = organizationService.getById(certificate.getOrgId());
        result.put("orgName", org != null ? org.getOrgName() : "未知机构");

        // 用户信息
        User user = userService.getById(certificate.getUserId());
        result.put("userName", user != null ? user.getName() : "未知用户");

        // 区块链信息
        if (certificate.getStatus() == 2) {  // 已上链
            result.put("chainTime", certificate.getChainTime());
            result.put("txHash", certificate.getTxHash());
            result.put("txHashShort", shortenHash(certificate.getTxHash()));
        }

        return ResponseVO.success("查询成功", result);
    }

    // 缩短哈希值显示
    private String shortenHash(String hash) {
        if (hash == null || hash.length() < 10) {
            return hash;
        }
        return hash.substring(0, 6) + "..." + hash.substring(hash.length() - 4);
    }
}