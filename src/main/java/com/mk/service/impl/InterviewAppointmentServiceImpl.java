package com.mk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mk.entity.InterviewAppointment;
import com.mk.mapper.InterviewAppointmentMapper;
import com.mk.service.IInterviewAppointmentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/17/16:09
 * @Description:
 */
@Service
public class InterviewAppointmentServiceImpl extends ServiceImpl<InterviewAppointmentMapper, InterviewAppointment> implements IInterviewAppointmentService {
    @Override
    public Long addAppointment(InterviewAppointment appointment) {
        save(appointment);
        return appointment.getId();
    }

    @Override
    public List<InterviewAppointment> listByUserId(Long userId) {
        LambdaQueryWrapper<InterviewAppointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewAppointment::getUserId, userId)
                .eq(InterviewAppointment::getIsDeleted, 0)
                .orderByDesc(InterviewAppointment::getAppointmentTime);//按预约时间倒叙
        return list(wrapper);
    }

    @Override
    public boolean updateAppointmentStatus(Long appointmentId, Integer status) {
        InterviewAppointment appointment = new InterviewAppointment();
        appointment.setId(appointmentId);
        appointment.setStatus(status);
        return updateById(appointment); // MyBatis-Plus内置更新方法
    }
}
