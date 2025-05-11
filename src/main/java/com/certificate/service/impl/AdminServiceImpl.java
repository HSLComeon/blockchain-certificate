package com.certificate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.certificate.common.constant.Constants;
import com.certificate.entity.Admin;
import com.certificate.mapper.AdminMapper;
import com.certificate.service.AdminService;
import com.certificate.util.JwtUtil;
import com.certificate.vo.admin.AdminInfoVO;
import com.certificate.vo.admin.AdminLoginVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public AdminInfoVO login(AdminLoginVO loginVO) {
        // 根据用户名查询管理员
        Admin admin = getByUsername(loginVO.getUsername());
        System.out.println("查询用户: " + loginVO.getUsername() + ", 结果: " + (admin != null ? "存在" : "不存在"));

        if (admin == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 输出密码信息以便调试
        System.out.println("输入密码: " + loginVO.getPassword());
        System.out.println("数据库密码: " + admin.getPassword());

        // 校验密码
        boolean matches = passwordEncoder.matches(loginVO.getPassword(), admin.getPassword());
        System.out.println("密码匹配结果: " + matches);

        if (!matches) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 校验管理员状态
        if (Constants.UserStatus.DISABLED == admin.getStatus()) {
            throw new RuntimeException("账号已被禁用");
        }

        // 生成token
        String token = jwtUtil.generateToken(admin.getId(), Constants.LoginType.ADMIN);

        // 转换为VO
        AdminInfoVO adminInfoVO = new AdminInfoVO();
        BeanUtils.copyProperties(admin, adminInfoVO);
        adminInfoVO.setToken(token);

        return adminInfoVO;
    }

    @Override
    public Admin getByUsername(String username) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, username);
        return getOne(wrapper);
    }
}