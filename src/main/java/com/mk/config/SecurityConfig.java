package com.mk.config;

import com.mk.interceptor.JwtAccessDeniedHandler;
import com.mk.interceptor.JwtAuthenticationEntryPoint;
import com.mk.interceptor.JwtValidationFilter;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;



/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/18/11:10
 * @Description:
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    @Resource
    private JwtValidationFilter jwtAuthenticationFilter; // 自定义JWT过滤器（已更正）
    @Resource
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint; // 认证失败处理器
    @Resource
    private JwtAccessDeniedHandler jwtAccessDeniedHandler; // 权限不足处理器

    // 密码编码器（BCrypt）
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 认证管理器
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 核心过滤器链配置
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(CsrfConfigurer::disable)
                // 关闭Session（JWT无状态）
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 权限控制
                .authorizeHttpRequests(auth -> auth
                        // 公开接口
                        .requestMatchers(new AntPathRequestMatcher("/api/java/login"), new AntPathRequestMatcher("/api/java/register")).permitAll()
                        // 管理员接口
                        .requestMatchers(new AntPathRequestMatcher("/api/java/admin/**")).hasRole("ADMIN")
                        // 普通用户接口
//                        .requestMatchers("/api/ai/interview/add").hasAuthority("interview:add")
                        // 其他AI接口需登录
                        .requestMatchers(new AntPathRequestMatcher("/api/java/**")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/test/**")).permitAll()
                        // 所有请求默认认证
                        .anyRequest().authenticated()
                )
                // ========== 4. 异常处理（不变） ==========
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                // ========== 5. 过滤器配置（不变） ==========
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
