package org.hcmu.hcmuserver.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.PeriodEnum;
import org.hcmu.hcmucommon.enumeration.WaitListEnum;
import org.hcmu.hcmupojo.entity.Department;
import org.hcmu.hcmupojo.entity.DoctorProfile;
import org.hcmu.hcmupojo.entity.DoctorSchedule;
import org.hcmu.hcmupojo.entity.User;
import org.hcmu.hcmupojo.entity.Waitlist;
import org.hcmu.hcmuserver.mapper.Waitlist.WaitlistMapper;
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
import java.time.LocalTime;
import java.util.List;

/**
 * 候补队列定时任务
 */
@Component
@Slf4j
public class WaitlistScheduledTask {

    @Autowired
    private WaitlistMapper waitlistMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DoctorProfileMapper doctorProfileMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private WaitlistService waitlistService;

    @Autowired
    private MailService mailService;

    /**
     * every 1 min
     * 超过锁号时间还没支付就无了
     */
    @Scheduled(fixedRate = 60000)
    public void handleExpiredNotifications() {
        log.debug("开始处理支付超时的候补...");

        // 查询所有超时的候补
        LambdaQueryWrapper<Waitlist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Waitlist::getStatus, WaitListEnum.NOTIFIED.getCode())
                .lt(Waitlist::getLockExpireTime, LocalDateTime.now());

        List<Waitlist> expiredList = waitlistMapper.selectList(wrapper);

        if (expiredList.isEmpty()) {
            log.debug("没有超时的候补记录");
            return;
        }

        log.info("发现 {} 条超时的候补记录", expiredList.size());

        for (Waitlist waitlist : expiredList) {
            try {
                // 更新状态为已取消
                waitlist.setStatus(WaitListEnum.CANCELLED.getCode());
                waitlist.setUpdateTime(LocalDateTime.now());
                waitlistMapper.updateById(waitlist);

                log.info("候补ID {} 支付超时，已取消", waitlist.getWaitlistId());

                // 发送超时邮件
                sendTimeoutEmail(waitlist);

                // 通知下一个候补
                boolean hasNext = waitlistService.notifyNextWaitlist(waitlist.getScheduleId());
                if (!hasNext) {
                    // 没有下一个候补，恢复号源
                    DoctorSchedule schedule = scheduleMapper.selectById(waitlist.getScheduleId());
                    if (schedule != null) {
                        schedule.setAvailableSlots(schedule.getAvailableSlots() + 1);
                        scheduleMapper.updateById(schedule);
                        log.info("候补队列已空，恢复排班ID {} 的号源", waitlist.getScheduleId());
                    }
                }
            } catch (Exception e) {
                log.error("处理超时候补ID {} 失败: {}", waitlist.getWaitlistId(), e.getMessage());
            }
        }

        log.info("支付超时处理完成");
    }

    /**
     * every 1 min
     * 排班开始前2小时内的候补都无掉了
     */
    @Scheduled(fixedRate = 60000)
    public void handleUpcomingSchedules() {
        log.debug("开始处理排班临期的候补...");

        LambdaQueryWrapper<Waitlist> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Waitlist::getStatus, WaitListEnum.WAITING.getCode(), WaitListEnum.NOTIFIED.getCode());

        List<Waitlist> waitlists = waitlistMapper.selectList(wrapper);

        if (waitlists.isEmpty()) {
            log.debug("没有需要检查的候补记录");
            return;
        }

        int cancelledCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Waitlist waitlist : waitlists) {
            try {
                DoctorSchedule schedule = scheduleMapper.selectById(waitlist.getScheduleId());
                if (schedule == null) {
                    continue;
                }

                PeriodEnum periodEnum = PeriodEnum.getEnumByCode(schedule.getSlotPeriod());
                if (periodEnum == null) {
                    continue;
                }

                String startTimeStr = periodEnum.getDesc().split("-")[0];
                LocalTime startTime = LocalTime.parse(startTimeStr);
                LocalDateTime scheduleDateTime = LocalDateTime.of(schedule.getScheduleDate(), startTime);

                // 检查是否在排班开始前2小时内
                LocalDateTime cutoffTime = scheduleDateTime.minusHours(2);
                if (now.isAfter(cutoffTime)) {
                    // 取消候补
                    waitlist.setStatus(WaitListEnum.CANCELLED.getCode());
                    waitlist.setUpdateTime(now);
                    waitlistMapper.updateById(waitlist);

                    log.info("候补ID {} 因排班临期自动取消（排班时间: {}）",
                            waitlist.getWaitlistId(), scheduleDateTime);

                    // 取消邮件
                    sendScheduleExpiredEmail(waitlist, schedule);

                    // 如果是 NOTIFIED 状态被取消，需要恢复号源
                    if (WaitListEnum.NOTIFIED.getCode().equals(waitlist.getStatus())) {
                        schedule.setAvailableSlots(schedule.getAvailableSlots() + 1);
                        scheduleMapper.updateById(schedule);
                        log.info("恢复排班ID {} 的号源", schedule.getScheduleId());
                    }

                    cancelledCount++;
                }
            } catch (Exception e) {
                log.error("处理临期候补ID {} 失败: {}", waitlist.getWaitlistId(), e.getMessage());
            }
        }

        if (cancelledCount > 0) {
            log.info("排班临期处理完成，共取消 {} 条候补", cancelledCount);
        } else {
            log.debug("没有临期的候补记录");
        }
    }

    private void sendTimeoutEmail(Waitlist waitlist) {
        try {
            User patient = userMapper.selectById(waitlist.getPatientUserId());
            if (patient == null || patient.getEmail() == null || patient.getEmail().isEmpty()) {
                return;
            }

            DoctorSchedule schedule = scheduleMapper.selectById(waitlist.getScheduleId());
            if (schedule == null) {
                return;
            }

            String periodDesc = "";
            PeriodEnum periodEnum = PeriodEnum.getEnumByCode(schedule.getSlotPeriod());
            if (periodEnum != null) {
                periodDesc = periodEnum.getDesc();
            }

            String subject = "候补支付超时通知";
            StringBuilder content = new StringBuilder();
            content.append("尊敬的 ").append(patient.getName()).append("，您好！\n\n");
            content.append("很遗憾，您的候补支付已超时。\n\n");
            content.append("原候补信息如下：\n");
            content.append("就诊日期：").append(schedule.getScheduleDate()).append("\n");
            content.append("就诊时段：").append(periodDesc).append("\n");
            content.append("\n您的候补资格已自动转给下一位患者。如需就诊，请重新预约或候补。\n");
            content.append("\n感谢您的理解！");

            mailService.sendNotification(subject, content.toString(), patient.getEmail());
            log.info("支付超时邮件已发送至: {}", patient.getEmail());
        } catch (Exception e) {
            log.error("发送支付超时邮件失败: {}", e.getMessage());
        }
    }

    /**
     * 发送排班临期取消邮件
     */
    private void sendScheduleExpiredEmail(Waitlist waitlist, DoctorSchedule schedule) {
        try {
            User patient = userMapper.selectById(waitlist.getPatientUserId());
            if (patient == null || patient.getEmail() == null || patient.getEmail().isEmpty()) {
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

            String subject = "候补自动取消通知";
            StringBuilder content = new StringBuilder();
            content.append("尊敬的 ").append(patient.getName()).append("，您好！\n\n");
            content.append("由于排班即将开始，您的候补已自动取消。\n\n");
            content.append("原候补信息如下：\n");
            content.append("就诊日期：").append(schedule.getScheduleDate()).append("\n");
            content.append("就诊时段：").append(periodDesc).append("\n");
            if (!departmentName.isEmpty()) {
                content.append("科室：").append(departmentName).append("\n");
            }
            content.append("医生：").append(doctorName).append("\n");
            content.append("\n如需就诊，请预约其他时段。\n");
            content.append("\n感谢您的理解！");

            mailService.sendNotification(subject, content.toString(), patient.getEmail());
            log.info("排班临期取消邮件已发送至: {}", patient.getEmail());
        } catch (Exception e) {
            log.error("发送排班临期取消邮件失败: {}", e.getMessage());
        }
    }
}