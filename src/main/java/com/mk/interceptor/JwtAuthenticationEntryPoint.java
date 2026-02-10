package com.mk.interceptor;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mk.common.Result;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.PrintWriter;
/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/18/17:52
 * @Description:
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Jackson对象转换器（Spring Boot已自动配置，可直接注入）
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // 核心判断：仅处理AuthenticationException（未认证/Token无效）
        if (authException instanceof AuthenticationException) {
            // 1. 设置响应头：JSON格式 + UTF-8编码
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            // 2. 设置HTTP状态码：401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            // 3. 构建错误返回结果
            Result<Void> result = Result.unauthorized("用户未认证或Token无效：" + authException.getMessage());

            // 4. 将结果写入响应体
            try (PrintWriter writer = response.getWriter()) {
                writer.write(objectMapper.writeValueAsString(result));
                writer.flush();
            }
        } else {
            // 其他RuntimeException：直接抛出，交由默认机制处理
            if (authException instanceof RuntimeException) {
                throw (RuntimeException) authException;
            }
            // 非RuntimeException：包装为RuntimeException抛出
            throw new RuntimeException("认证流程异常", authException);
        }
    }
}