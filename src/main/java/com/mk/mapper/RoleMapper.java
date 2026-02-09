package com.mk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mk.entity.SysMenu;
import com.mk.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/18/10:29
 * @Description:
 */
@Mapper
public interface RoleMapper extends BaseMapper<SysRole> {
    List<SysMenu> selectMenusByRoleId(Long roleId);
}
