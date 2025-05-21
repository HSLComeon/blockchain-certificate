package com.certificate.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        // JwtAuthorizationFilter设置的principal为userId(Long)
        if (authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        // 兼容其他情况
        if (authentication.getPrincipal() instanceof String) {
            try {
                return Long.valueOf((String) authentication.getPrincipal());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}