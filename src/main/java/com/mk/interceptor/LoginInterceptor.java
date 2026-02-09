//package com.mk.interceptor;
//
//import com.mk.entity.SysUser;
//import com.mk.service.ISysUserService;
//import com.mk.util.UserContext;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.apache.kerby.kerberos.provider.token.JwtUtil;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
///**
// * Created with IntelliJ IDEA.
// *
// * @Author: milk
// * @Date: 2025/12/17/19:17
// * @Description:
// */
//@Component
//public class LoginInterceptor implements HandlerInterceptor {
//    private final UserContext userContext;
//    private final ISysUserService sysUserService;
//    // 假设你有JWT工具类解析Token
//    private final JwtUtil jwtUtil;
//
//    public LoginInterceptor(UserContext userContext, ISysUserService sysUserService, JwtUtil jwtUtil) {
//        this.userContext = userContext;
//        this.sysUserService = sysUserService;
//        this.jwtUtil = jwtUtil;
//    }
//
//    // 请求进入时：解析Token → 查询用户 → 设置到UserContext
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        // 1. 从请求头获取Token（如Authorization: Bearer xxx）
//        String token = request.getHeader("Authorization");
//        if (token != null && token.startsWith("Bearer ")) {
//            token = token.substring(7);
//            // 2. 解析Token获取用户ID
//            Long userId = jwtUtil.getUserIdFromToken(token);
//            if (userId != null) {
//                // 3. 查询完整用户信息
//                SysUser sysUser = sysUserService.getById(userId);
//                if (sysUser != null) {
//                    // 4. 设置到UserContext（业务层/Tool可直接获取）
//                    userContext.setCurrentUser(sysUser);
//                }
//            }
//        }
//        return true;
//    }
//
//    // 请求结束时：清除UserContext，避免ThreadLocal内存泄漏
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        userContext.clear();
//    }
//}
