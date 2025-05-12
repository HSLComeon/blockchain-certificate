package com.certificate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.certificate.entity.Admin;
import com.certificate.mapper.AdminMapper;
import com.certificate.service.AdminService;
import com.certificate.util.JwtUtil;
import com.certificate.vo.admin.AdminInfoVO;
import com.certificate.vo.admin.AdminLoginVO;
import com.certificate.vo.admin.AdminRegisterVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public AdminInfoVO login(AdminLoginVO loginVO) {
        // 查询用户
        Admin admin = getByUsername(loginVO.getUsername());
        if (admin == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginVO.getPassword(), admin.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查账号状态
        if (admin.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        // 生成token
        String token = jwtUtil.generateToken(admin.getId(), "admin");
        // 返回用户信息
        AdminInfoVO adminInfoVO = new AdminInfoVO();
        BeanUtils.copyProperties(admin, adminInfoVO);
        adminInfoVO.setToken(token);

        return adminInfoVO;
    }

    @Override
    public boolean register(AdminRegisterVO registerVO) {
        // 检查用户名是否已存在
        Admin existAdmin = getByUsername(registerVO.getUsername());
        if (existAdmin != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建管理员对象
        Admin admin = new Admin();
        BeanUtils.copyProperties(registerVO, admin);

        // 加密密码
        admin.setPassword(passwordEncoder.encode(registerVO.getPassword()));

        // 设置其他字段
        admin.setStatus(1); // 默认启用
        admin.setCreateTime(new Date());
        admin.setUpdateTime(new Date());

        // 保存到数据库
        return save(admin);
    }

    @Override
    public Admin getByUsername(String username) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, username);
        return getOne(wrapper);
    }
}