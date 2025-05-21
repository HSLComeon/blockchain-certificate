package com.certificate.controller;

import com.certificate.entity.User;
import com.certificate.service.UserService;
import com.certificate.util.JwtUtil;
import com.certificate.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

// UserController.java
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/profile")
    public ResponseVO<User> getProfile(HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseVO.error("未登录或Token无效");
        }
        User user = userService.getById(userId);
        if (user == null) {
            return ResponseVO.error("用户不存在");
        }
        return ResponseVO.success("获取成功", user);
    }
}