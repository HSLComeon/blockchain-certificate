package com.certificate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.certificate.entity.User;
import com.certificate.vo.user.UserInfoVO;
import com.certificate.vo.user.UserLoginVO;
import com.certificate.vo.user.UserRegisterVO;

public interface UserService extends IService<User> {

    /**
     * 用户登录
     * @param loginVO 登录信息
     * @return 用户信息和token
     */
    UserInfoVO login(UserLoginVO loginVO);

    /**
     * 用户注册
     * @param registerVO 注册信息
     * @return 是否成功
     */
    boolean register(UserRegisterVO registerVO);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    User getByUsername(String username);
}