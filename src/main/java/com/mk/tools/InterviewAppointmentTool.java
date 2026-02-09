package com.mk.tools;

import com.mk.customagentannotation.SecurityProperty;
import com.mk.customagentannotation.ToolsSecurity;
import com.mk.entity.InterviewAppointment;
import com.mk.entity.SysUserDetails;
import com.mk.entity.UserSafe;
import com.mk.service.IInterviewAppointmentService;
import com.mk.util.UserContext;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/17/16:26
 * @Description:
 */
@Component
public class InterviewAppointmentTool {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private IInterviewAppointmentService interviewAppointmentService;
    @Tool("新增面试预约信息，参数说明：" +
            "userId-用户ID（数字，优先从对话上下文中获取，或者利用已有的tools进行获取），" +
            "companyName-公司名称，" +
            "position-面试岗位，" +
            "appointmentTime-预约时间（格式yyyy-MM-dd HH:mm:ss），" +
            "remark-备注（可选）")
    @ToolsSecurity(properties = {
            @SecurityProperty(key = "action", value = "Read"),
            @SecurityProperty(key = "security_level", value = "3"),
    })
    public String addInterviewAppointment(Long userId, String companyName, String position,
                                          String appointmentTime, String remark) {
        System.out.println("------------------------执行Tools:新增面试----------------");
        try {
            // 2. 使用自定义格式化器解析空格分隔的时间字符串
            LocalDateTime time = LocalDateTime.parse(appointmentTime, DATETIME_FORMATTER);

            InterviewAppointment appointment = new InterviewAppointment();
            appointment.setUserId(userId);
            appointment.setCompanyName(companyName);
            appointment.setPosition(position);
            appointment.setAppointmentTime(time);
            appointment.setRemark(remark == null ? "" : remark);
            appointment.setStatus(0); // 默认待面试

            Long appointmentId = interviewAppointmentService.addAppointment(appointment);
            return "面试预约新增成功！预约ID：" + appointmentId + "，状态：待面试";
        } catch (DateTimeParseException e) {
            // 精准提示时间格式问题
            return "预约新增失败：时间格式错误！请使用 yyyy-MM-dd HH:mm:ss 格式（示例：2025-12-20 10:00:00），错误详情：" + e.getMessage();
        } catch (Exception e) {
            // 其他异常（如用户不存在、参数为空）
            return "预约新增失败：" + e.getMessage();
        }
    }

    /**
     * 查询用户所有面试预约（Agent调用的核心工具）
     * @param userId 用户ID（必填）
     * @return 格式化的预约列表
     */
    @Tool("查询指定用户的所有面试预约信息，参数：userId-用户ID（数字）")
    public String listInterviewAppointment(Long userId) {
        System.out.println("------------------------执行Tools:查询面试预约----------------");
        List<InterviewAppointment> list = interviewAppointmentService.listByUserId(userId);
        if (list.isEmpty()) {
            return "用户ID：" + userId + " 暂无面试预约记录";
        }

        // 格式化返回结果（方便Agent返回给用户）
        StringBuilder sb = new StringBuilder();
        sb.append("用户ID：").append(userId).append(" 的面试预约列表：\n");
        for (InterviewAppointment app : list) {
            String statusDesc = switch (app.getStatus()) {
                case 0 -> "待面试";
                case 1 -> "已完成";
                case 2 -> "已取消";
                case 3 -> "已延期";
                default -> "未知状态";
            };
            sb.append("预约ID：").append(app.getId())
                    .append(" | 公司：").append(app.getCompanyName())
                    .append(" | 岗位：").append(app.getPosition())
                    .append(" | 时间：").append(app.getAppointmentTime())
                    .append(" | 状态：").append(statusDesc)
                    .append(" | 备注：").append(app.getRemark()).append("\n");
        }
        return sb.toString();
    }
    @Tool("在用户未明确说出个人信息已经从上下文不能获取当前用户信时息，通过该工具查询当前用户信息，参数包括：" +
            "userId：用户ID" +
            "username：用户名" +
            "enabled:用户是否启用" +
            "authorities：用户的角色列表" +
            "perms：用户的权限列表")
    public UserSafe getCurrentUser() {
        UserSafe userDetails = UserContext.getCurrentUser();
        return userDetails;
    }

    @ToolsSecurity(properties = {
//            @SecurityProperty(key = "action", value = "Read"),
            @SecurityProperty(key = "security_level", value = "4"),
    })
    @Tool("获取当前时间，格式：yyyy-MM-dd HH:mm:ss")
    public String getCurrentTime(){
        System.out.println("------------------------执行Tools:获取当前时间----------------");
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

}
