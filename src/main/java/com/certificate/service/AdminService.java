package com.certificate.service;

import com.certificate.dto.AdminLoginDTO;
import com.certificate.dto.AdminRegisterDTO;
import com.certificate.entity.Admin;

import java.util.Map;

public interface AdminService {
    Map<String, String> login(AdminLoginDTO adminLoginDTO);

    void register(AdminRegisterDTO adminRegisterDTO);

    Map<String, Object> getInfo();

    Admin getAdminByUsername(String username);
}