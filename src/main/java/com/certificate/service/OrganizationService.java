package com.certificate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.certificate.entity.Organization;
import com.certificate.vo.org.OrgInfoVO;
import com.certificate.vo.org.OrgLoginVO;
import com.certificate.vo.org.OrgRegisterVO;

public interface OrganizationService extends IService<Organization> {

    /**
     * 机构登录
     * @param loginVO 登录信息
     * @return 机构信息和token
     */
    OrgInfoVO login(OrgLoginVO loginVO);

    /**
     * 机构注册
     * @param registerVO 注册信息
     * @return 是否成功
     */
    boolean register(OrgRegisterVO registerVO);

    /**
     * 根据用户名查询机构
     * @param username 用户名
     * @return 机构信息
     */
    Organization getByUsername(String username);
}