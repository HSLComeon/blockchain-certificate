package com.certificate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.certificate.entity.Admin;
import com.certificate.vo.admin.AdminInfoVO;
import com.certificate.vo.admin.AdminLoginVO;

public interface AdminService extends IService<Admin> {

    /**
     * 管理员登录
     * @param loginVO 登录信息
     * @return 管理员信息和token
     */
    AdminInfoVO login(AdminLoginVO loginVO);

    /**
     * 根据用户名查询管理员
     * @param username 用户名
     * @return 管理员信息
     */
    Admin getByUsername(String username);
}