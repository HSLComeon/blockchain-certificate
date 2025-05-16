package com.certificate.controller;

import com.certificate.service.AdminService;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.admin.AdminInfoVO;
import com.certificate.vo.admin.AdminLoginVO;
import com.certificate.vo.admin.AdminRegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin/auth")  // 注意：移除/api前缀
public class AdminAuthController {

    @Autowired
    private AdminService adminService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public ResponseVO<AdminInfoVO> login(@Valid @RequestBody AdminLoginVO loginVO) {
        System.out.println("AdminAuthController - 收到登录请求: " + loginVO.getUsername());
        AdminInfoVO adminInfoVO = adminService.login(loginVO);
        return ResponseVO.success("登录成功", adminInfoVO);
    }

    /**
     * 管理员注册
     */
    @PostMapping("/register")
    public ResponseVO<Boolean> register(@Valid @RequestBody AdminRegisterVO registerVO) {
        System.out.println("AdminAuthController - 收到注册请求: " + registerVO.getUsername());
        boolean result = adminService.register(registerVO);
        return ResponseVO.success("注册成功", result);
    }
}