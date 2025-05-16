package com.certificate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.certificate.common.constant.Constants;
import com.certificate.entity.Organization;
import com.certificate.mapper.OrganizationMapper;
import com.certificate.service.OrganizationService;
import com.certificate.util.JwtUtil;
import com.certificate.vo.org.OrgInfoVO;
import com.certificate.vo.org.OrgLoginVO;
import com.certificate.vo.org.OrgRegisterVO;
import com.certificate.vo.org.OrgUpdateStatusVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements OrganizationService {

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public OrgInfoVO login(OrgLoginVO loginVO) {
        // 根据用户名查询机构
        Organization org = getByUsername(loginVO.getUsername());
        if (org == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 校验密码
        if (!passwordEncoder.matches(loginVO.getPassword(), org.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 校验机构状态
        if (Constants.OrganizationStatus.DISABLED == org.getStatus()) {
            throw new RuntimeException("账号已被禁用");
        }

        // 如果机构待审核或已拒绝，提示用户
        if (Constants.OrganizationStatus.PENDING == org.getStatus()) {
            throw new RuntimeException("账号正在审核中，请耐心等待");
        }

        if (Constants.OrganizationStatus.REJECTED == org.getStatus()) {
            throw new RuntimeException("账号审核未通过，请联系管理员");
        }

        // 生成token
        String token = jwtUtil.generateToken(org.getId(), Constants.LoginType.ORG);

        // 转换为VO
        OrgInfoVO orgInfoVO = new OrgInfoVO();
        BeanUtils.copyProperties(org, orgInfoVO);
        orgInfoVO.setToken(token);

        return orgInfoVO;
    }

    @Override
    public boolean register(OrgRegisterVO registerVO) {
        // 校验用户名是否存在
        Organization existOrg = getByUsername(registerVO.getUsername());
        if (existOrg != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建机构对象
        Organization org = new Organization();
        BeanUtils.copyProperties(registerVO, org);

        // 设置默认值
        org.setPassword(passwordEncoder.encode(registerVO.getPassword()));
        org.setStatus(Constants.OrganizationStatus.PENDING); // 默认待审核
        org.setCreateTime(new Date());
        org.setUpdateTime(new Date());

        // 保存到数据库
        return save(org);
    }

    @Override
    public Organization getByUsername(String username) {
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getUsername, username);
        return getOne(wrapper);
    }

    @Override
    public IPage<Organization> getOrgList(Integer status, String keyword, Integer pageNum, Integer pageSize) {
        Page<Organization> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();

        // 根据状态筛选
        if (status != null) {
            wrapper.eq(Organization::getStatus, status);
        }

        // 根据关键字搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(Organization::getOrgName, keyword)
                    .or()
                    .like(Organization::getUsername, keyword)
                    .or()
                    .like(Organization::getContactPerson, keyword)
            );
        }

        // 按创建时间降序排序
        wrapper.orderByDesc(Organization::getCreateTime);

        return page(page, wrapper);
    }

    @Override
    public boolean updateOrgStatus(OrgUpdateStatusVO updateStatusVO) {
        Organization org = getById(updateStatusVO.getId());
        if (org == null) {
            throw new RuntimeException("机构不存在");
        }

        org.setStatus(updateStatusVO.getStatus());
        org.setUpdateTime(new Date());

        return updateById(org);
    }

    @Override
    public List<Organization> getRecentOrgs(int limit) {
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Organization::getCreateTime);
        wrapper.last("LIMIT " + limit);
        return list(wrapper);
    }

    @Override
    public int countOrgByStatus(Integer status) {
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getStatus, status);
        return (int) count(wrapper);
    }

}