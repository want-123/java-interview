package com.mk.interceptor;

import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.RegisteredPayload;
import com.mk.util.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/18/13:30
 * @Description:
 */
@Component
public class JwtValidationFilter extends OncePerRequestFilter {
    @Value("${spring.security.jwt.secret}")
    private String SECRET_KEY;
    @Autowired
    private JwtUtils jwtUtils;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. 从请求头获取Token
        String authHeader = request.getHeader("Authorization");
        try{
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // 校验 Token 是否有效
                String token = authHeader.substring(7);
                if (JWTUtil.verify(token, SECRET_KEY.getBytes())) {
                    System.out.println("Token is valid.");
                    // 解析 Token 获取有效载荷
                    JWT jwt = JWTUtil.parseToken(token);
                    JSONObject payloads = jwt.getPayloads();
                    // Token 有效将其解析为 Authentication 对象，并设置到 Spring Security 上下文中
                    System.out.println("Payloads: " + payloads);
                    Authentication authentication = jwtUtils.getAuthentication(payloads);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // Token 无效，直接返回响应
                    return;
                }
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}
