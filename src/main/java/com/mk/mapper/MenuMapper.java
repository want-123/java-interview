package com.mk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mk.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/18/10:33
 * @Description:
 */
@Mapper
public interface MenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> selectByUserId(Long userId);
}
