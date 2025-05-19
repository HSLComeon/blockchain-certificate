// blockchain-certificate/src/main/java/com/certificate/controller/OrgCertificateTypeController.java
package com.certificate.controller;

import com.certificate.entity.CertificateType;
import com.certificate.service.CertificateTypeService;
import com.certificate.util.JwtUtil;
import com.certificate.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/org/certificate-types")
public class OrgCertificateTypeController {

    @Autowired
    private CertificateTypeService certificateTypeService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取证书类型列表（机构可用的）
     */
    @GetMapping
    public ResponseVO<List<CertificateType>> getEnabledCertificateTypes(HttpServletRequest request) {
        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        try {
            // 获取启用状态的证书类型
            List<CertificateType> types = certificateTypeService.getEnabledTypes();
            return ResponseVO.success("获取成功", types);
        } catch (Exception e) {
            log.error("获取证书类型列表失败: {}", e.getMessage());
            return ResponseVO.error("获取证书类型列表失败: " + e.getMessage());
        }
    }
}