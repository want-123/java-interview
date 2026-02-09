package com.mk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/17/15:54
 * @Description:
 */
@Data
@TableName("interview_appointment")
public class InterviewAppointment {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 面试公司名称 */
    private String companyName;

    /** 面试岗位 */
    private String position;

    /** 预约面试时间 */
    private LocalDateTime appointmentTime;

    /** 预约状态：0-待面试 1-已完成 2-已取消 3-已延期 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建时间（MyBatis-Plus自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间（MyBatis-Plus自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除（MyBatis-Plus） */
    @TableLogic
    private Integer isDeleted;
}
