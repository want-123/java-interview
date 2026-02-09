package com.mk.util;

import com.mk.entity.SysUserDetails;
import com.mk.entity.UserSafe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/17/18:46
 * @Description:
 */
@Component
public class UserContext {
    public static UserSafe getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SysUserDetails userDetails = (SysUserDetails) authentication.getPrincipal() ;
        return new UserSafe(userDetails);
    }
//    public static void clear() {
//        currentUser.remove();
//    }
}
