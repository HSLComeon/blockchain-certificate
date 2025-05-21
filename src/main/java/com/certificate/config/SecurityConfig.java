package com.certificate.config;

import com.certificate.filter.JwtAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthorizationFilter jwtAuthorizationFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 添加调试日志
        System.out.println("SecurityConfig - 配置安全规则");

        http
                .cors().and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 放行不需要认证的路径
                .antMatchers("/api/admin/auth/**", "/api/org/auth/**", "/api/user/auth/**").permitAll()
                .antMatchers("/admin/auth/**", "/org/auth/**", "/user/auth/**").permitAll() // 也放行不带/api前缀的路径
                // 临时放行所有admin和org相关接口
                .antMatchers("/api/admin/**", "/api/org/**").permitAll()
                // 用户API接口
                .antMatchers("/api/user/auth/**").permitAll() // 用户认证相关接口放行
                // 允许已登录的普通用户、管理员、机构管理员访问用户相关接口
                .antMatchers("/api/user/**").hasAnyRole("USER", "ADMIN", "ORG")
                .antMatchers("/admin/**", "/org/**").permitAll() // 也放行不带/api前缀的路径
                // 公共API接口
                .antMatchers("/api/public/**", "/public/**").permitAll()
                // 允许OPTIONS请求
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
                .and()
                .httpBasic().disable()
                .formLogin().disable();

        // 添加JWT过滤器
        http.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        System.out.println("SecurityConfig - 已放行路径: /api/admin/auth/**, /api/org/auth/**, /api/user/auth/**, /api/admin/**, /api/org/**");
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000",
                "http://localhost:5173", "http://127.0.0.1:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}