package com.certificate.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.certificate.entity.Organization;
import com.certificate.vo.org.OrgInfoVO;
import com.certificate.vo.org.OrgLoginVO;
import com.certificate.vo.org.OrgRegisterVO;
import com.certificate.vo.org.OrgUpdateStatusVO;

import java.util.List;

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

    /**
     * 分页查询机构列表
     * @param status 状态
     * @param keyword 关键字
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    IPage<Organization> getOrgList(Integer status, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 更新机构状态
     * @param updateStatusVO 更新状态VO
     * @return 是否成功
     */
    boolean updateOrgStatus(OrgUpdateStatusVO updateStatusVO);

    /**
     * 获取最近注册的机构
     * @param limit 限制数量
     * @return 机构列表
     */
    List<Organization> getRecentOrgs(int limit);

    /**
     * 根据状态统计机构数量
     * @param status 状态
     * @return 数量
     */
    int countOrgByStatus(Integer status);
}