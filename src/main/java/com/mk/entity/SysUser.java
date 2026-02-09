package com.mk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/17/19:14
 * @Description:
 */
@Data
@TableName(value = "sys_user") // 显式指定表名，避免驼峰自动转换问题
public class SysUser {

    /**
     * 用户主键ID（关联interview_appointment.user_id）
     */
    @TableId(type = IdType.AUTO) // 对应表的AUTO_INCREMENT自增策略
    private Long id;

    /**
     * 用户账号（唯一，登录用）
     */
    @TableField(value = "username")
    private String username;

    /**
     * 密码（加密存储，如BCrypt）
     */
    @TableField(value = "password")
    private String password;

    /**
     * 用户昵称
     */
    @TableField(value = "nickname")
    private String nickname;

    /**
     * 手机号（唯一）
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 邮箱（唯一）
     */
    @TableField(value = "email")
    private String email;

    /**
     * 头像URL
     */
    @TableField(value = "avatar")
    private String avatar;

    /**
     * 用户状态：1-启用 0-禁用（默认启用）
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 最后登录时间
     */
    @TableField(value = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间（MyBatis-Plus自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT) // 仅插入时自动填充
    private LocalDateTime createTime;

    /**
     * 更新时间（MyBatis-Plus自动填充）
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE) // 插入+更新时自动填充
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删 1-已删（MyBatis-Plus）
     */
    @TableLogic // MyBatis-Plus逻辑删除注解，自动处理删除/查询过滤
    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @TableField(exist = false)
    private List<SysRole> roles;

    // 可选：添加状态常量，方便业务层使用（避免魔法值）
    public static final Integer STATUS_ENABLE = 1; // 启用
    public static final Integer STATUS_DISABLE = 0; // 禁用

    public static final Integer IS_DELETED_NO = 0; // 未删除
    public static final Integer IS_DELETED_YES = 1; // 已删除
}
