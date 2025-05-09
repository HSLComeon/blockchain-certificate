package com.certificate.controller;

import com.certificate.common.api.ApiResult;
import com.certificate.dto.InstitutionLoginDTO;
import com.certificate.dto.InstitutionRegisterDTO;
import com.certificate.service.InstitutionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "机构管理员接口")
@RestController
@RequestMapping("/api/institution")
public class InstitutionController {

    @Autowired
    private InstitutionService institutionService;

    @ApiOperation("机构管理员登录")
    @PostMapping("/login")
    public ApiResult<Map<String, String>> login(@Validated @RequestBody InstitutionLoginDTO institutionLoginDTO) {
        Map<String, String> tokenMap = institutionService.login(institutionLoginDTO);
        return ApiResult.success(tokenMap);
    }

    @ApiOperation("机构管理员注册")
    @PostMapping("/register")
    public ApiResult<Void> register(@Validated @RequestBody InstitutionRegisterDTO institutionRegisterDTO) {
        institutionService.register(institutionRegisterDTO);
        return ApiResult.success();
    }

    @ApiOperation("获取当前机构管理员信息")
    @GetMapping("/info")
    @PreAuthorize("hasRole('INSTITUTION')")
    public ApiResult<Map<String, Object>> getInfo() {
        Map<String, Object> institutionInfo = institutionService.getInfo();
        return ApiResult.success(institutionInfo);
    }
}