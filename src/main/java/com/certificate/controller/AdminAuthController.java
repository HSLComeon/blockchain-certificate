package com.certificate.controller;

import com.certificate.service.AdminService;
import com.certificate.service.LogService;
import com.certificate.util.IpUtil;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.admin.AdminInfoVO;
import com.certificate.vo.admin.AdminLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/admin/auth") // 注意这里不需要添加/api前缀，因为已在application.yml中设置了context-path
public class AdminAuthController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private LogService logService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public ResponseVO<AdminInfoVO> login(@Valid @RequestBody AdminLoginVO loginVO, HttpServletRequest request) {
        try {
            System.out.println("接收到管理员登录请求: " + loginVO.getUsername());
            AdminInfoVO adminVO = adminService.login(loginVO);

            // 添加登录日志
            logService.addLog(
                    "login",
                    "管理员登录",
                    adminVO.getUsername(),
                    adminVO.getId(),
                    IpUtil.getIpAddress(request),
                    "success",
                    "管理员" + adminVO.getUsername() + "成功登录系统，登录IP：" + IpUtil.getIpAddress(request)
            );

            return ResponseVO.success("登录成功", adminVO);
        } catch (Exception e) {
            e.printStackTrace();
            // 添加登录失败日志
            logService.addLog(
                    "login",
                    "管理员登录失败",
                    loginVO.getUsername(),
                    null,
                    IpUtil.getIpAddress(request),
                    "failed",
                    "用户" + loginVO.getUsername() + "尝试登录系统失败，原因：" + e.getMessage() + "，登录IP：" + IpUtil.getIpAddress(request)
            );

            return ResponseVO.error(e.getMessage());
        }
    }
}