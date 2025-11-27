package org.hcmu.hcmuserver.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.PeriodEnum;
import org.hcmu.hcmupojo.entity.Appointment;
import org.hcmu.hcmupojo.entity.Department;
import org.hcmu.hcmupojo.entity.DoctorProfile;
import org.hcmu.hcmupojo.entity.DoctorSchedule;
import org.hcmu.hcmupojo.entity.User;
import org.hcmu.hcmuserver.mapper.appointment.AppointmentMapper;
import org.hcmu.hcmuserver.mapper.department.DepartmentMapper;
import org.hcmu.hcmuserver.mapper.doctorprofile.DoctorProfileMapper;
import org.hcmu.hcmuserver.mapper.schedule.ScheduleMapper;
import org.hcmu.hcmuserver.mapper.user.UserMapper;
import org.hcmu.hcmuserver.service.MailService;
import org.hcmu.hcmuserver.service.WaitlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约定时任务
 * 处理预约支付超时逻辑
 */
@Component
@Slf4j
public class AppointmentScheduledTask {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DoctorProfileMapper doctorProfileMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private MailService mailService;

    @Autowired
    private WaitlistService waitlistService;

    /**
     * every 1 min
     * 处理支付超时的预约
     * 更改状态，释放号源，通知候补
     */
    @Scheduled(fixedRate = 60000)
    public void handleExpiredAppointments() {
        log.debug("开始处理支付超时的预约...");

        // 查询待支付且已超时的
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getStatus, 1)
                .lt(Appointment::getLockExpireTime, LocalDateTime.now());

        List<Appointment> expiredAppointments = appointmentMapper.selectList(wrapper);

        if (expiredAppointments.isEmpty()) {
            log.debug("没有支付超时的预约记录");
            return;
        }

        log.info("发现 {} 条支付超时的预约记录", expiredAppointments.size());

        for (Appointment appointment : expiredAppointments) {
            try {

                // 过期
                appointment.setStatus(5);
                appointment.setUpdateTime(LocalDateTime.now());
                appointmentMapper.updateById(appointment);

                log.info("预约ID {} 支付超时，已设置为取消状态", appointment.getAppointmentId());

                DoctorSchedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
                if (schedule != null) {
                    schedule.setAvailableSlots(schedule.getAvailableSlots() + 1);
                    scheduleMapper.updateById(schedule);
                    log.info("恢复排班ID {} 的号源，当前可用: {}", schedule.getScheduleId(), schedule.getAvailableSlots());

                    boolean hasWaitlist = waitlistService.notifyNextWaitlist(appointment.getScheduleId());
                    if (hasWaitlist) {
                        log.info("已通知排班ID {} 的候补患者", appointment.getScheduleId());
                    }
                }

                sendPaymentTimeoutEmail(appointment);

            } catch (Exception e) {
                log.error("处理超时预约ID {} 失败: {}", appointment.getAppointmentId(), e.getMessage(), e);
            }
        }

        log.info("支付超时处理完成，共处理 {} 条预约", expiredAppointments.size());
    }

    /**
     * 发送支付超时通知邮件
     */
    private void sendPaymentTimeoutEmail(Appointment appointment) {
        try {
            User patient = userMapper.selectById(appointment.getPatientUserId());
            if (patient == null || patient.getEmail() == null || patient.getEmail().isEmpty()) {
                log.debug("预约ID {} 的患者没有邮箱，跳过发送邮件", appointment.getAppointmentId());
                return;
            }

            DoctorSchedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
            if (schedule == null) {
                log.warn("预约ID {} 的排班不存在", appointment.getAppointmentId());
                return;
            }

            User doctor = userMapper.selectById(schedule.getDoctorUserId());
            String doctorName = doctor != null ? doctor.getName() : "未知";

            String departmentName = "";
            LambdaQueryWrapper<DoctorProfile> profileWrapper = new LambdaQueryWrapper<>();
            profileWrapper.eq(DoctorProfile::getUserId, schedule.getDoctorUserId())
                    .last("LIMIT 1");
            DoctorProfile doctorProfile = doctorProfileMapper.selectOne(profileWrapper);
            if (doctorProfile != null && doctorProfile.getDepartmentId() != null) {
                Department department = departmentMapper.selectById(doctorProfile.getDepartmentId());
                if (department != null) {
                    departmentName = department.getName();
                }
            }

            String periodDesc = "";
            PeriodEnum periodEnum = PeriodEnum.getEnumByCode(schedule.getSlotPeriod());
            if (periodEnum != null) {
                periodDesc = periodEnum.getDesc();
            }

            String subject = "预约支付超时通知";
            StringBuilder content = new StringBuilder();
            content.append("尊敬的 ").append(patient.getName()).append("，您好！\n\n");
            content.append("很遗憾，您的预约因未在规定时间内支付已自动取消。\n\n");
            content.append("原预约信息如下：\n");
            content.append("预约号：").append(appointment.getAppointmentNo()).append("\n");
            content.append("就诊日期：").append(schedule.getScheduleDate()).append("\n");
            content.append("就诊时段：").append(periodDesc).append("\n");
            if (!departmentName.isEmpty()) {
                content.append("科室：").append(departmentName).append("\n");
            }
            content.append("医生：").append(doctorName).append("\n");
            content.append("挂号费：¥").append(appointment.getActualFee()).append("\n");
            content.append("\n该号源已释放，如需就诊请重新预约。\n");
            content.append("\n感谢您的理解！");

            mailService.sendNotification(subject, content.toString(), patient.getEmail());
            log.info("支付超时邮件已发送至: {}", patient.getEmail());
        } catch (Exception e) {
            log.error("发送支付超时邮件失败: {}", e.getMessage(), e);
        }
    }
}