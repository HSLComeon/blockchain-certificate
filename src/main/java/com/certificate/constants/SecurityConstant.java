package com.certificate.common.constant;

public class SecurityConstant {

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_INSTITUTION = "INSTITUTION";

    public static final String ROLE_PREFIX = "ROLE_";

    // 不需要认证的URL
    public static final String[] WHITE_LIST = {
            "/api/admin/login",
            "/api/admin/register",
            "/api/institution/login",
            "/api/institution/register",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/v2/api-docs",
            "/doc.html"
    };
}