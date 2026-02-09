package com.mk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mk.entity.InterviewAppointment;
import com.mk.entity.SysUser;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/17/19:31
 * @Description:
 */
public interface ISysUserService extends IService<SysUser> {
    Long addSysUser(SysUser sysUser);

    /**
     * 查询用户所有面试预约
     * @param userId 用户ID
     * @return 预约列表
     */
    SysUser listByUserId(Long userId);
    SysUser getUserByUsername(String username);
}
