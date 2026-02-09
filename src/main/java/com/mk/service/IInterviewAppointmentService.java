package com.mk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mk.entity.InterviewAppointment;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/17/16:03
 * @Description:
 */
public interface IInterviewAppointmentService extends IService<InterviewAppointment> {
    /**
     * 新增面试预约
     * @param appointment 预约信息（需包含userId、companyName、position、appointmentTime）
     * @return 新增成功的预约ID
     */
    Long addAppointment(InterviewAppointment appointment);

    /**
     * 查询用户所有面试预约
     * @param userId 用户ID
     * @return 预约列表
     */
    List<InterviewAppointment> listByUserId(Long userId);

    /**
     * 更新预约状态
     * @param appointmentId 预约ID
     * @param status 新状态（0-待面试 1-已完成 2-已取消 3-已延期）
     * @return 是否更新成功
     */
    boolean updateAppointmentStatus(Long appointmentId, Integer status);
}
