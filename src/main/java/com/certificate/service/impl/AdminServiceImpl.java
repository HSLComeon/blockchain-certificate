package com.certificate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.certificate.common.api.ResultCode;
import com.certificate.common.exception.BusinessException;
import com.certificate.dto.AdminLoginDTO;
import com.certificate.dto.AdminRegisterDTO;
import com.certificate.entity.Admin;
import com.certificate.mapper.AdminMapper;
import com.certificate.service.AdminService;
import com.certificate.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AdminServiceImpl(AdminMapper adminMapper,
                            PasswordEncoder passwordEncoder,
                            AuthenticationManager authenticationManager,
                            JwtUtil jwtUtil) {
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Map<String, String> login(AdminLoginDTO adminLoginDTO) {
        // 进行身份验证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        adminLoginDTO.getUsername(),
                        adminLoginDTO.getPassword()
                )
        );

        // 生成token
        String token = jwtUtil.generateToken(adminLoginDTO.getUsername(), "ADMIN");

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        return tokenMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(AdminRegisterDTO adminRegisterDTO) {
        // 检查用户名是否已存在
        if (isUsernameExists(adminRegisterDTO.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_EXIST.getMessage());
        }

        // 创建新管理员
        Admin admin = new Admin();
        BeanUtils.copyProperties(adminRegisterDTO, admin);

        // 加密密码
        admin.setPassword(passwordEncoder.encode(adminRegisterDTO.getPassword()));

        // 保存到数据库
        adminMapper.insert(admin);
    }

    @Override
    public Map<String, Object> getInfo() {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询用户信息
        Admin admin = getAdminByUsername(username);

        Map<String, Object> info = new HashMap<>();
        info.put("username", admin.getUsername());
        info.put("role", "ADMIN");
        return info;
    }

    @Override
    public Admin getAdminByUsername(String username) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, username);
        return adminMapper.selectOne(wrapper);
    }

    private boolean isUsernameExists(String username) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, username);
        return adminMapper.selectCount(wrapper) > 0;
    }
}