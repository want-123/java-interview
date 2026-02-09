package com.mk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mk.entity.SysMenu;
import com.mk.entity.SysRole;
import com.mk.entity.SysUser;
import com.mk.mapper.RoleMapper;
import com.mk.mapper.SysUserMapper;
import com.mk.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/17/19:33
 * @Description:
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Override
    public Long addSysUser(SysUser sysUser) {
        save(sysUser);
        return sysUser.getId();
    }

    @Override
    public SysUser listByUserId(Long userId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getId, userId)
                .eq(SysUser::getIsDeleted, 0);
        SysUser sysUser = getOne(wrapper);
        if(sysUser == null) throw new RuntimeException("用户不存在");
        if(sysUser.getStatus() == SysUser.STATUS_DISABLE) throw new RuntimeException("用户已禁用");
        // 查询用户角色
        List<SysRole> roles = sysUserMapper.selectRolesByUserId(userId);
        // 查询角色菜单
        roles.forEach(role -> {
            List<SysMenu> menus = roleMapper.selectMenusByRoleId(role.getRoleId());
            role.setMenus(menus);
        });
        sysUser.setRoles(roles);
        return sysUser;
    }

    @Override
    public SysUser getUserByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username)
                .eq(SysUser::getStatus, SysUser.STATUS_ENABLE);//1-启用
        SysUser sysUser = getOne(wrapper);
        if(sysUser == null) throw new RuntimeException("用户不存在");
        if(sysUser.getStatus() == SysUser.STATUS_DISABLE) throw new RuntimeException("用户已禁用");
        // 查询用户角色
        List<SysRole> roles = sysUserMapper.selectRolesByUserId(sysUser.getId());
        // 查询角色菜单
        roles.forEach(role -> {
            List<SysMenu> menus = roleMapper.selectMenusByRoleId(role.getRoleId());
            role.setMenus(menus);
        });
        sysUser.setRoles(roles);
        return sysUser;
    }
}
