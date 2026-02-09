package com.mk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mk.entity.SysRole;
import com.mk.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/17/19:31
 * @Description:
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    List<SysRole> selectRolesByUserId(Long userId);
}
