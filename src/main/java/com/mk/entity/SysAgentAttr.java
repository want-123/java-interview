package com.mk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/19/16:18
 * @Description:
 */
@Data
@TableName("sys_agent_attr")
public class SysAgentAttr {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long agentId;
    private String attrKey;
    private String attrValue;

}
