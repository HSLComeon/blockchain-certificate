package com.certificate.filter;

import com.certificate.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;

    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // 获取请求头中的Authorization
        String authHeader = request.getHeader(tokenHeader);
        // 打印调试信息
        System.out.println("JWT过滤器 - 请求路径: " + request.getRequestURI());
        System.out.println("JWT过滤器 - 认证头: " + (authHeader != null ? "存在" : "不存在"));

        // 判断是否有token
        if (authHeader != null && authHeader.startsWith(tokenHead)) {
            // 截取token
            String authToken = authHeader.substring(tokenHead.length());
            try {
                // 验证token
                if (jwtUtil.validateToken(authToken)) {
                    // 获取用户信息
                    Long userId = jwtUtil.getIdFromToken(authToken);
                    String userType = jwtUtil.getTypeFromToken(authToken);

                    // 设置认证信息
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userType.toUpperCase()))
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("JWT过滤器 - 认证成功: userId=" + userId + ", userType=" + userType);
                } else {
                    System.out.println("JWT过滤器 - Token无效");
                }
            } catch (Exception e) {
                System.err.println("JWT过滤器 - 验证Token异常: " + e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }
}