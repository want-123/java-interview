package com.mk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/19/16:09
 * @Description:
 */

@Data
@TableName("sys_agent")
public class SysAgent {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("agent_name")
    private String name;
    @TableField("des")
    private String description;
    @TableField(exist = false)
    private Map<String, String> attrs;
}
