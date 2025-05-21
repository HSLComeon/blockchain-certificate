package com.certificate.controller;

import com.certificate.service.UserService;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.user.UserInfoVO;
import com.certificate.vo.user.UserLoginVO;
import com.certificate.vo.user.UserRegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/user/auth")
public class UserAuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseVO<UserInfoVO> login(@Valid @RequestBody UserLoginVO loginVO) {
        UserInfoVO userInfoVO = userService.login(loginVO);
        return ResponseVO.success("登录成功", userInfoVO);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseVO<Boolean> register(@Valid @RequestBody UserRegisterVO registerVO) {
        boolean result = userService.register(registerVO);
        return ResponseVO.success("注册成功", result);
    }
}