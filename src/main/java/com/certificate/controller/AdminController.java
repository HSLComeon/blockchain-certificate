package com.certificate.controller;

import com.certificate.common.api.ApiResult;
import com.certificate.dto.AdminLoginDTO;
import com.certificate.dto.AdminRegisterDTO;
import com.certificate.service.AdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "系统管理员接口")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @ApiOperation("管理员登录")
    @PostMapping("/login")
    public ApiResult<Map<String, String>> login(@Validated @RequestBody AdminLoginDTO adminLoginDTO) {
        Map<String, String> tokenMap = adminService.login(adminLoginDTO);
        return ApiResult.success(tokenMap);
    }

    @ApiOperation("管理员注册")
    @PostMapping("/register")
    public ApiResult<Void> register(@Validated @RequestBody AdminRegisterDTO adminRegisterDTO) {
        adminService.register(adminRegisterDTO);
        return ApiResult.success();
    }

    @ApiOperation("获取当前管理员信息")
    @GetMapping("/info")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Map<String, Object>> getInfo() {
        Map<String, Object> adminInfo = adminService.getInfo();
        return ApiResult.success(adminInfo);
    }
}