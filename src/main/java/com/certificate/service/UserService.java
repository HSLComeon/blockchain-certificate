// blockchain-certificate/src/main/java/com/certificate/service/UserService.java
package com.certificate.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.certificate.entity.User;
import com.certificate.vo.user.OrgUserCreateVO;
import com.certificate.vo.user.OrgUserUpdateVO;
import com.certificate.vo.user.UserInfoVO;
import com.certificate.vo.user.UserLoginVO;
import com.certificate.vo.user.UserRegisterVO;

import java.util.List;

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

    /**
     * 创建机构用户
     * @param createVO 创建信息
     * @param orgId 机构ID
     * @return 是否成功
     */
    boolean createOrgUser(OrgUserCreateVO createVO, Long orgId);

    /**
     * 更新机构用户
     * @param id 用户ID
     * @param updateVO 更新信息
     * @param orgId 机构ID
     * @return 是否成功
     */
    boolean updateOrgUser(Long id, OrgUserUpdateVO updateVO, Long orgId);

    /**
     * 获取机构用户列表
     * @param orgId 机构ID
     * @param keyword 关键字
     * @param page 分页参数
     * @return 分页结果
     */
    IPage<User> getOrgUserList(Long orgId, String keyword, Page<User> page);

    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 状态
     * @param orgId 机构ID
     * @return 是否成功
     */
    boolean updateUserStatus(Long id, Integer status, Long orgId);

    /**
     * 根据机构ID统计用户数量
     * @param orgId 机构ID
     * @return 用户数量
     */
    int countByOrgId(Long orgId);

    /**
     * 根据机构ID和状态统计用户数量
     * @param orgId 机构ID
     * @param status 状态
     * @return 用户数量
     */
    int countByOrgIdAndStatus(Long orgId, Integer status);
}