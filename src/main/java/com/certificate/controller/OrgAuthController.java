package com.certificate.controller;

import com.certificate.service.OrganizationService;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.org.OrgInfoVO;
import com.certificate.vo.org.OrgLoginVO;
import com.certificate.vo.org.OrgRegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/org/auth")  // 注意：移除/api前缀
public class OrgAuthController {

    @Autowired
    private OrganizationService organizationService;

    /**
     * 机构登录
     */
    @PostMapping("/login")
    public ResponseVO<OrgInfoVO> login(@Valid @RequestBody OrgLoginVO loginVO) {
        System.out.println("OrgAuthController - 收到登录请求: " + loginVO.getUsername());
        OrgInfoVO orgInfoVO = organizationService.login(loginVO);
        return ResponseVO.success("登录成功", orgInfoVO);
    }

    /**
     * 机构注册
     */
    @PostMapping("/register")
    public ResponseVO<Boolean> register(@Valid @RequestBody OrgRegisterVO registerVO) {
        System.out.println("OrgAuthController - 收到注册请求: " + registerVO.getUsername());
        boolean result = organizationService.register(registerVO);
        return ResponseVO.success("注册成功", result);
    }
}