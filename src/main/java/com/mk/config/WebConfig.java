//package com.mk.config;
//
//import com.mk.interceptor.LoginInterceptor;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
///**
// * Created with IntelliJ IDEA.
// *
// * @Author: milk
// * @Date: 2025/12/17/19:19
// * @Description:
// */
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//    private final LoginInterceptor loginInterceptor;
//
//    public WebConfig(LoginInterceptor loginInterceptor) {
//        this.loginInterceptor = loginInterceptor;
//    }
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        // 拦截所有AI相关接口，自动设置UserContext
//        registry.addInterceptor(loginInterceptor)
//                .addPathPatterns("/api/ai/**"); // 你的AI接口路径
//    }
//}