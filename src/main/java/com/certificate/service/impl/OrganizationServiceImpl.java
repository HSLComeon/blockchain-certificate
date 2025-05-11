package com.certificate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.certificate.common.constant.Constants;
import com.certificate.entity.Organization;
import com.certificate.mapper.OrganizationMapper;
import com.certificate.service.OrganizationService;
import com.certificate.util.JwtUtil;
import com.certificate.vo.org.OrgInfoVO;
import com.certificate.vo.org.OrgLoginVO;
import com.certificate.vo.org.OrgRegisterVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

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
        if (Constants.UserStatus.DISABLED == org.getStatus()) {
            throw new RuntimeException("账号已被禁用");
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
        // 校验用户名是否已存在
        if (getByUsername(registerVO.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 校验机构名称是否已存在
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getOrgName, registerVO.getOrgName());
        if (count(wrapper) > 0) {
            throw new RuntimeException("机构名称已存在");
        }

        // 创建机构
        Organization org = new Organization();
        BeanUtils.copyProperties(registerVO, org);
        // 加密密码
        org.setPassword(passwordEncoder.encode(registerVO.getPassword()));
        // 设置状态
        org.setStatus(Constants.UserStatus.ENABLED);
        // 设置创建时间和更新时间
        Date now = new Date();
        org.setCreateTime(now);
        org.setUpdateTime(now);

        return save(org);
    }

    @Override
    public Organization getByUsername(String username) {
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getUsername, username);
        return getOne(wrapper);
    }
}