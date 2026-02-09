package com.mk.entity;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/18/15:23
 * @Description:
 */
@Data
@NoArgsConstructor
public class UserSafe {
    private Long userId;

    private String username;

    private Boolean enabled;

    private Collection<SimpleGrantedAuthority> authorities;

    private Set<String> perms;

    public UserSafe(SysUserDetails user) {
        this.userId = user.getUserId();
        this.authorities = (Collection<SimpleGrantedAuthority>) user.getAuthorities();
        this.username = user.getUsername();
        this.enabled = ObjectUtil.equal(user.getEnabled(), 1);

    }
}
