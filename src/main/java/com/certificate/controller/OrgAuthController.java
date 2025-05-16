package com.certificate.controller;

import com.certificate.service.LogService;
import com.certificate.service.OrganizationService;
import com.certificate.util.IpUtil;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.org.OrgInfoVO;
import com.certificate.vo.org.OrgLoginVO;
import com.certificate.vo.org.OrgRegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/org/auth")  // 注意：保持原有路径
public class OrgAuthController {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private LogService logService;

    /**
     * 机构登录
     */
    @PostMapping("/login")
    public ResponseVO<OrgInfoVO> login(@Valid @RequestBody OrgLoginVO loginVO, HttpServletRequest request) {
        System.out.println("OrgAuthController - 收到登录请求: " + loginVO.getUsername());
        try {
            OrgInfoVO orgVO = organizationService.login(loginVO);

            // 添加登录日志
            logService.addLog(
                    "login",
                    "机构管理员登录",
                    orgVO.getUsername(),
                    orgVO.getId(),
                    IpUtil.getIpAddress(request),
                    "success",
                    "机构管理员" + orgVO.getUsername() + "成功登录系统，登录IP：" + IpUtil.getIpAddress(request)
            );

            return ResponseVO.success("登录成功", orgVO);
        } catch (Exception e) {
            // 添加登录失败日志
            logService.addLog(
                    "login",
                    "机构管理员登录失败",
                    loginVO.getUsername(),
                    null,
                    IpUtil.getIpAddress(request),
                    "failed",
                    "用户" + loginVO.getUsername() + "尝试登录系统失败，原因：" + e.getMessage() + "，登录IP：" + IpUtil.getIpAddress(request)
            );

            return ResponseVO.error(e.getMessage());
        }
    }

    /**
     * 机构注册
     */
    @PostMapping("/register")
    public ResponseVO<Boolean> register(@Valid @RequestBody OrgRegisterVO registerVO, HttpServletRequest request) {
        System.out.println("OrgAuthController - 收到注册请求: " + registerVO.getUsername());
        try {
            boolean result = organizationService.register(registerVO);

            // 添加注册日志
            logService.addLog(
                    "operation",
                    "机构注册",
                    registerVO.getUsername(),
                    null,
                    IpUtil.getIpAddress(request),
                    "success",
                    "机构" + registerVO.getOrgName() + "注册成功，联系人：" + registerVO.getUsername() + "，手机号码：" + registerVO.getContactPhone()
            );

            return ResponseVO.success("注册成功，请等待管理员审核", result);
        } catch (Exception e) {
            return ResponseVO.error(e.getMessage());
        }
    }
}