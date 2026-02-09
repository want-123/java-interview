package com.mk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/19/16:10
 * @Description:
 */
@Data
@TableName("sys_policy")
public class SysPolicy {

    @TableId(type = IdType.AUTO)
    private Long policyId;
    private String policyName;
    private String targetResource;
    private String conditionExpression;
}