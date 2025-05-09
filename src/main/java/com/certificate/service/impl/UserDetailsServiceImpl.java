package com.certificate.security;

import com.certificate.entity.Admin;
import com.certificate.entity.Institution;
import com.certificate.service.AdminService;
import com.certificate.service.InstitutionService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AdminService adminService;
    private final InstitutionService institutionService;

    public UserDetailsServiceImpl(AdminService adminService, InstitutionService institutionService) {
        this.adminService = adminService;
        this.institutionService = institutionService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 先尝试查找管理员
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            return new User(
                    admin.getUsername(),
                    admin.getPassword(),
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        }

        // 再尝试查找机构管理员
        Institution institution = institutionService.getInstitutionByUsername(username);
        if (institution != null) {
            return new User(
                    institution.getUsername(),
                    institution.getPassword(),
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_INSTITUTION"))
            );
        }

        throw new UsernameNotFoundException("用户名不存在");
    }
}