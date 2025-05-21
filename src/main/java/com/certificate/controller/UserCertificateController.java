package com.certificate.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.certificate.service.CertificateService;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.certificate.CertificateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.certificate.util.SecurityUtils;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user/certificates")
public class UserCertificateController {

    @Autowired
    private CertificateService certificateService;

    @GetMapping
    public ResponseVO<IPage<CertificateVO>> getUserCertificates(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        // 从token中获取用户ID
        Long userId = SecurityUtils.getCurrentUserId();
        // 如果你的JwtUtil有getUserIdFromRequest方法，也可以用
        // Long userId = jwtUtil.getUserIdFromRequest(request);

        if (userId == null) {
            return ResponseVO.error("未登录或token无效");
        }
        IPage<CertificateVO> page = certificateService.getCertificateListByUserId(userId, pageNum, pageSize);
        return ResponseVO.success("获取成功", page);
    }
}