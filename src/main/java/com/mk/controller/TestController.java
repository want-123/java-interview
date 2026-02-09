package com.mk.controller;


import com.mk.entity.LoginResult;
import com.mk.entity.SysUser;
import com.mk.entity.SysUserDetails;
import com.mk.service.ISysUserService;
import com.mk.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/18/14:31
 * @Description:
 */
@RestController
@RequestMapping("/api/test")
@Tag(name = "接口测试")
public class TestController {
    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private JwtUtils jwtUtils;

    @Operation(summary = "生成测试Token")
    @GetMapping("/gtoken")
    public LoginResult generateTestToken(@RequestParam String username){
        SysUser user = sysUserService.getUserByUsername(username);
        SysUserDetails userDetails = new SysUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, // 已构造的用户详情
                null,        // 无需密码
                userDetails.getAuthorities()  // 已确定的权限
        );
        String token = jwtUtils.createToken(authentication);
        return LoginResult.builder()
                .tokenType("Bearer")
                .accessToken(token)
                .build();
    }


}
