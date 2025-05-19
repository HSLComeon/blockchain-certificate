package com.certificate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.certificate.common.constant.Constants;
import com.certificate.entity.User;
import com.certificate.mapper.UserMapper;
import com.certificate.service.UserService;
import com.certificate.util.JwtUtil;
import com.certificate.vo.user.*;
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
    // 在UserServiceImpl.java文件中添加以下方法

    @Override
    public boolean createOrgUser(OrgUserCreateVO createVO, Long orgId) {
        // 检查用户名是否已存在
        if (getByUsername(createVO.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建新用户
        User user = new User();
        BeanUtils.copyProperties(createVO, user);

        // 设置机构ID
        user.setOrgId(orgId);

        // 加密密码
        user.setPassword(passwordEncoder.encode(createVO.getPassword()));

        // 设置状态和时间
        user.setStatus(Constants.UserStatus.ENABLED);
        Date now = new Date();
        user.setCreateTime(now);
        user.setUpdateTime(now);

        return save(user);
    }

    @Override
    public boolean updateOrgUser(Long id, OrgUserUpdateVO updateVO, Long orgId) {
        // 获取用户
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证用户所属机构
        if (!user.getOrgId().equals(orgId)) {
            throw new RuntimeException("无权操作此用户");
        }

        // 更新基本信息
        if (updateVO.getName() != null) {
            user.setName(updateVO.getName());
        }
        if (updateVO.getIdCard() != null) {
            user.setIdCard(updateVO.getIdCard());
        }
        if (updateVO.getPhone() != null) {
            user.setPhone(updateVO.getPhone());
        }
        if (updateVO.getEmail() != null) {
            user.setEmail(updateVO.getEmail());
        }
        if (updateVO.getGender() != null) {
            user.setGender(updateVO.getGender());
        }

        // 更新密码（如果提供了新密码）
        if (updateVO.getPassword() != null && !updateVO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateVO.getPassword()));
        }

        user.setUpdateTime(new Date());

        return updateById(user);
    }

    @Override
    public IPage<User> getOrgUserList(Long orgId, String keyword, Page<User> page) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOrgId, orgId);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getName, keyword)
                    .or().like(User::getPhone, keyword));
        }

        wrapper.orderByDesc(User::getCreateTime);

        return page(page, wrapper);
    }

    @Override
    public boolean updateUserStatus(Long id, Integer status, Long orgId) {
        // 获取用户
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证用户所属机构
        if (!user.getOrgId().equals(orgId)) {
            throw new RuntimeException("无权操作此用户");
        }

        // 更新状态
        user.setStatus(status);
        user.setUpdateTime(new Date());

        return updateById(user);
    }

    @Override
    public int countByOrgId(Long orgId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOrgId, orgId);
        return (int) count(wrapper);
    }

    @Override
    public int countByOrgIdAndStatus(Long orgId, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOrgId, orgId)
                .eq(User::getStatus, status);
        return (int) count(wrapper);
    }
}