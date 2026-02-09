package com.mk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mk.entity.SysAgent;
import com.mk.entity.SysAgentAttr;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/19/16:16
 * @Description:
 */
@Mapper
public interface AgentMapper extends BaseMapper<SysAgent> {
    List<SysAgentAttr> selectUserAttrByAgentId(Long agentId);
}
