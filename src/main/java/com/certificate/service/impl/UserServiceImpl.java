package com.certificate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.certificate.common.constant.Constants;
import com.certificate.entity.User;
import com.certificate.mapper.UserMapper;
import com.certificate.service.UserService;
import com.certificate.util.JwtUtil;
import com.certificate.vo.user.UserInfoVO;
import com.certificate.vo.user.UserLoginVO;
import com.certificate.vo.user.UserRegisterVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserInfoVO login(UserLoginVO loginVO) {
        // 根据用户名查询用户
        User user = getByUsername(loginVO.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 校验密码
        if (!passwordEncoder.matches(loginVO.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 校验用户状态
        if (Constants.UserStatus.DISABLED == user.getStatus()) {
            throw new RuntimeException("账号已被禁用");
        }

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), Constants.LoginType.USER);

        // 转换为VO
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO);
        userInfoVO.setToken(token);

        return userInfoVO;
    }

    @Override
    public boolean register(UserRegisterVO registerVO) {
        // 校验用户名是否已存在
        if (getByUsername(registerVO.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(registerVO, user);
        // 加密密码
        user.setPassword(passwordEncoder.encode(registerVO.getPassword()));
        // 设置状态
        user.setStatus(Constants.UserStatus.ENABLED);
        // 设置创建时间和更新时间
        Date now = new Date();
        user.setCreateTime(now);
        user.setUpdateTime(now);

        return save(user);
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }
}