package com.certificate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.certificate.common.api.ResultCode;
import com.certificate.common.exception.BusinessException;
import com.certificate.dto.InstitutionLoginDTO;
import com.certificate.dto.InstitutionRegisterDTO;
import com.certificate.entity.Institution;
import com.certificate.mapper.InstitutionMapper;
import com.certificate.service.InstitutionService;
import com.certificate.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class InstitutionServiceImpl implements InstitutionService {

    private final InstitutionMapper institutionMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public InstitutionServiceImpl(InstitutionMapper institutionMapper,
                                  PasswordEncoder passwordEncoder,
                                  AuthenticationManager authenticationManager,
                                  JwtUtil jwtUtil) {
        this.institutionMapper = institutionMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Map<String, String> login(InstitutionLoginDTO institutionLoginDTO) {
        // 进行身份验证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        institutionLoginDTO.getUsername(),
                        institutionLoginDTO.getPassword()
                )
        );

        // 生成token
        String token = jwtUtil.generateToken(institutionLoginDTO.getUsername(), "INSTITUTION");

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        return tokenMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(InstitutionRegisterDTO institutionRegisterDTO) {
        // 检查用户名是否已存在
        if (isUsernameExists(institutionRegisterDTO.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_EXIST.getMessage());
        }

        // 创建新机构管理员
        Institution institution = new Institution();
        institution.setUsername(institutionRegisterDTO.getUsername());
        institution.setPassword(passwordEncoder.encode(institutionRegisterDTO.getPassword()));
        institution.setInstitutionName(institutionRegisterDTO.getInstitutionName());
        institution.setDescription(institutionRegisterDTO.getDescription());
        institution.setStatus(0); // 设置初始状态为待审核

        // 保存到数据库
        institutionMapper.insert(institution);
    }

    @Override
    public Map<String, Object> getInfo() {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询用户信息
        Institution institution = getInstitutionByUsername(username);

        Map<String, Object> info = new HashMap<>();
        info.put("username", institution.getUsername());
        info.put("institutionName", institution.getInstitutionName());
        info.put("role", "INSTITUTION");
        return info;
    }

    @Override
    public Institution getInstitutionByUsername(String username) {
        LambdaQueryWrapper<Institution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Institution::getUsername, username);
        return institutionMapper.selectOne(wrapper);
    }

    private boolean isUsernameExists(String username) {
        LambdaQueryWrapper<Institution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Institution::getUsername, username);
        return institutionMapper.selectCount(wrapper) > 0;
    }
}