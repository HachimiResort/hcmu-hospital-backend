package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;

import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.OpRuleEnum;
import org.hcmu.hcmucommon.enumeration.PeriodEnum;
import org.hcmu.hcmucommon.enumeration.RedisEnum;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmucommon.utils.RedisUtil;
import org.hcmu.hcmupojo.dto.AppointmentDTO;
import org.hcmu.hcmupojo.dto.AppointmentDTO.AppointmentListDTO;
import org.hcmu.hcmupojo.dto.OperationRuleDTO.RuleInfo;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.entity.*;
import org.hcmu.hcmuserver.mapper.appointment.AppointmentMapper;
import org.hcmu.hcmuserver.mapper.schedule.ScheduleMapper;
import org.hcmu.hcmuserver.service.AppointmentService;
import org.hcmu.hcmuserver.service.MailService;
import org.hcmu.hcmuserver.service.OperationRuleService;
import org.hcmu.hcmuserver.service.UserService;
import org.hcmu.hcmuserver.service.WaitlistService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@EnableScheduling
public class AppointmentServiceImpl extends MPJBaseServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    // 允许预期的时间
    private static final int OVERDUE_DELAY_MINUTES = 1;
    private static final long CHECK_IN_TOKEN_TTL_MINUTES = 5L;

    @Autowired
    private UserService userService;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private OperationRuleService operationRuleService;

    @Autowired
    private MailService mailService;

    @Autowired
    private WaitlistService waitlistService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Result<PageDTO<AppointmentDTO.AppointmentListDTO>> getAppointments(AppointmentDTO.AppointmentGetRequestDTO requestDTO) {
        MPJLambdaWrapper<Appointment> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.selectAll(Appointment.class)
                // 关联患者用户表
                .leftJoin(User.class, User::getUserId, Appointment::getPatientUserId)
                .selectAs(User::getUserName, "patientUserName")
                .selectAs(User::getName, "patientName")
                .selectAs(User::getPhone, "patientPhone")
                // 关联排班表
                .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                .selectAs(DoctorSchedule::getScheduleDate, "scheduleDate")
                .selectAs(DoctorSchedule::getSlotType, "slotType")
                .selectAs(DoctorSchedule::getSlotPeriod, "slotPeriod")
                // 关联医生用户表（通过排班表的doctor_user_id）
                .leftJoin(User.class, "doctor_user", User::getUserId, DoctorSchedule::getDoctorUserId)
                .select("doctor_user.name as doctorName")
                // 关联医生档案表（获取职称）
                .leftJoin(DoctorProfile.class, DoctorProfile::getUserId, DoctorSchedule::getDoctorUserId)
                .selectAs(DoctorProfile::getTitle, "doctorTitle")
                .selectAs(DoctorProfile::getUserId, "doctorUserId")
                // 关联科室表（通过医生档案表的department_id）
                .leftJoin(Department.class, Department::getDepartmentId, DoctorProfile::getDepartmentId)
                .selectAs(Department::getName, "departmentName")
                .eq(ObjectUtils.isNotEmpty(requestDTO.getScheduleId()),
                        Appointment::getScheduleId, requestDTO.getScheduleId())
                .eq(ObjectUtils.isNotEmpty(requestDTO.getPatientUserId()),
                        Appointment::getPatientUserId, requestDTO.getPatientUserId())
                .eq(requestDTO.getIsDeleted() != null,
                        Appointment::getIsDeleted, requestDTO.getIsDeleted())
                .orderByDesc(Appointment::getCreateTime);


        // 执行分页查询
        IPage<AppointmentDTO.AppointmentListDTO> page = baseMapper.selectJoinPage(
                new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize()),
                AppointmentDTO.AppointmentListDTO.class,
                queryWrapper
        );

        return Result.success(new PageDTO<>(page));
    }

    @Override
    public Result<AppointmentDTO.AppointmentListDTO> getAppointmentById(Long appointmentId) {
        if (appointmentId == null || appointmentId <= 0) {
            return Result.error("预约ID不能为空");
        }

        // 构建查询条件
        MPJLambdaWrapper<Appointment> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.selectAll(Appointment.class)
                // 关联患者用户表
                .leftJoin(User.class, User::getUserId, Appointment::getPatientUserId)
                .selectAs(User::getUserName, "patientUserName")
                .selectAs(User::getName, "patientName")
                .selectAs(User::getPhone, "patientPhone")
                // 关联排班表
                .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                .selectAs(DoctorSchedule::getScheduleDate, "scheduleDate")
                .selectAs(DoctorSchedule::getSlotType, "slotType")
                .selectAs(DoctorSchedule::getSlotPeriod, "slotPeriod")
                // 关联医生用户表（通过排班表的doctor_user_id）
                .leftJoin(User.class, "doctor_user", User::getUserId, DoctorSchedule::getDoctorUserId)
                .select("doctor_user.name as doctorName")
                // 关联医生档案表（获取职称）
                .leftJoin(DoctorProfile.class, DoctorProfile::getUserId, DoctorSchedule::getDoctorUserId)
                .selectAs(DoctorProfile::getTitle, "doctorTitle")
                .selectAs(DoctorProfile::getUserId, "doctorUserId")
                // 关联科室表（通过医生档案表的department_id）
                .leftJoin(Department.class, Department::getDepartmentId, DoctorProfile::getDepartmentId)
                .selectAs(Department::getName, "departmentName")
                .eq(Appointment::getAppointmentId, appointmentId);

        AppointmentDTO.AppointmentListDTO detailDTO = baseMapper.selectJoinOne(
                AppointmentDTO.AppointmentListDTO.class,
                queryWrapper
        );

        if (detailDTO == null) {
            return Result.error("预约记录不存在或已被删除");
        }

        return Result.success(detailDTO);
    }

    @Override
    public Result<PageDTO<AppointmentDTO.AppointmentListDTO>> getAppointmentsByPatientUserId(
            Long patientUserId) {

        User user = userService.getById(patientUserId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 构建查询条件
        MPJLambdaWrapper<Appointment> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.selectAll(Appointment.class)
                .leftJoin(User.class, User::getUserId, Appointment::getPatientUserId)
                .selectAs(User::getUserName, "patientUserName")
                .selectAs(User::getName, "patientName")
                .selectAs(User::getPhone, "patientPhone")
                .eq(Appointment::getPatientUserId, patientUserId)
                .orderByDesc(Appointment::getCreateTime);

        // 执行分页查询
        IPage<AppointmentDTO.AppointmentListDTO> page = baseMapper.selectJoinPage(
                new Page<>(1, 20),
                AppointmentDTO.AppointmentListDTO.class,
                queryWrapper
        );

        if (page.getRecords().isEmpty()) {
            return Result.success("该用户暂无预约记录", new PageDTO<>(page));
        }

        return Result.success(new PageDTO<>(page));
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<AppointmentListDTO> cancelAppointment(Long appointmentId, String reason) {
        Appointment appointment = baseMapper.selectById(appointmentId);
        if (appointment == null || appointment.getIsDeleted() == 1) {
            return Result.error("预约记录不存在");
        }

        if (appointment.getStatus() == null || appointment.getStatus() == 3 || appointment.getStatus() == 4 || appointment.getStatus() == 5 || appointment.getStatus() == 6) {
            return Result.error("当前预约状态不允许取消");
        }

        // 是否需要填写取消原因
        RuleInfo needReasonRule = operationRuleService.getRuleValueByCode(OpRuleEnum.CANCEL_NEED_REASON);
        if (needReasonRule != null && needReasonRule.getEnabled() == 1 && needReasonRule.getValue() == 1) {
            if (reason == null || reason.trim().isEmpty()) {
                return Result.error("取消预约需要填写原因");
            }
        }

        DoctorSchedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
        if (schedule == null) {
            return Result.error("关联的排班信息不存在");
        }

        // 禁止取消时限规则
        RuleInfo forbidCancelRule = operationRuleService.getRuleValueByCode(
            OpRuleEnum.CANCEL_FORBID_CANCEL_HOURS
        );

        if (forbidCancelRule != null && forbidCancelRule.getEnabled() == 1) {
            Integer forbidHours = forbidCancelRule.getValue();


            PeriodEnum periodEnum = PeriodEnum.getEnumByCode(schedule.getSlotPeriod());
            if (periodEnum != null) {
                String startTimeStr = periodEnum.getDesc().split("-")[0];
                LocalTime startTime = LocalTime.parse(startTimeStr);
                LocalDateTime scheduleDateTime = LocalDateTime.of(schedule.getScheduleDate(), startTime);

                LocalDateTime forbidCancelTime = scheduleDateTime.minusHours(forbidHours);

                log.info("预约ID {} - 就诊时间: {}, 禁止取消截止时间: {}, 当前时间: {}",
                         appointmentId, scheduleDateTime, forbidCancelTime, LocalDateTime.now());

                // 是否在禁止取消时限内
                if (LocalDateTime.now().isAfter(forbidCancelTime)) {
                    return Result.error("就诊前 " + forbidHours + " 小时内不允许取消预约");
                }
            }
        }

        appointment.setStatus(5);  // 状态改为已取消
        appointment.setCancellationTime(LocalDateTime.now());
        appointment.setCancellationReason(reason);
        baseMapper.updateById(appointment);



        // 查询完整的预约信息（包含关联的患者、排班、医生、科室信息）
        MPJLambdaWrapper<Appointment> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.selectAll(Appointment.class)

                .leftJoin(User.class, User::getUserId, Appointment::getPatientUserId)
                .selectAs(User::getUserName, "patientUserName")
                .selectAs(User::getName, "patientName")
                .selectAs(User::getPhone, "patientPhone")
                .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                .selectAs(DoctorSchedule::getScheduleDate, "scheduleDate")
                .selectAs(DoctorSchedule::getSlotType, "slotType")
                .selectAs(DoctorSchedule::getSlotPeriod, "slotPeriod")
                .leftJoin(User.class, "doctor_user", User::getUserId, DoctorSchedule::getDoctorUserId)
                .select("doctor_user.name as doctorName")
                .leftJoin(DoctorProfile.class, DoctorProfile::getUserId, DoctorSchedule::getDoctorUserId)
                .selectAs(DoctorProfile::getTitle, "doctorTitle")
                .selectAs(DoctorProfile::getUserId, "doctorUserId")
                .leftJoin(Department.class, Department::getDepartmentId, DoctorProfile::getDepartmentId)
                .selectAs(Department::getName, "departmentName")
                .eq(Appointment::getAppointmentId, appointmentId);

        AppointmentListDTO dto = baseMapper.selectJoinOne(
                AppointmentListDTO.class,
                queryWrapper
        );


        User user = userService.getById(appointment.getPatientUserId());
        String userEmail = user.getEmail();

        // 获取时段信息
        String periodDesc = "";
        PeriodEnum periodEnum = PeriodEnum.getEnumByCode(schedule.getSlotPeriod());
        if (periodEnum != null) {
            periodDesc = periodEnum.getDesc();
        }
    
        String subject = "挂号取消通知";
        StringBuilder content = new StringBuilder();
        content.append("尊敬的 ").append(user.getName()).append("，您好！\n\n");
        content.append("您的预约已成功取消。\n");
        content.append("预约信息如下：\n");
        content.append("预约号：").append(appointment.getAppointmentNo()).append("\n");
        content.append("就诊日期：").append(schedule.getScheduleDate()).append("\n");
        content.append("就诊时段：").append(periodDesc).append("\n");
        if (dto.getDepartmentName() != null && !dto.getDepartmentName().isEmpty()) {
            content.append("科室：").append(dto.getDepartmentName()).append("\n");
        }
        if (dto.getDoctorName() != null) {
            content.append("医生：").append(dto.getDoctorName()).append("\n");
        }
        content.append("挂号费：¥").append(dto.getActualFee()).append("\n");
        content.append("\n如有任何疑问，请随时联系我们的客服团队。\n");

        mailService.sendNotification(subject, content.toString(), userEmail);
        log.info("预约取消邮件已发送至: {}", userEmail);

        boolean hasWaitlist = waitlistService.notifyNextWaitlist(appointment.getScheduleId());
        if (hasWaitlist) {

            log.info("已通知排班ID {} 的下一个候补患者，号源保持锁定", appointment.getScheduleId());
        } else {

            schedule.setAvailableSlots(schedule.getAvailableSlots() + 1);
            scheduleMapper.updateById(schedule);
            log.info("排班ID {} 没有候补患者，号源已恢复，当前可用号源: {}", appointment.getScheduleId(), schedule.getAvailableSlots());
        }

        return Result.success("取消预约成功", dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<AppointmentListDTO> payAppointment(Long appointmentId) {

        Appointment appointment = baseMapper.selectById(appointmentId);
        if (appointment == null || appointment.getIsDeleted() == 1) {
            return Result.error("预约记录不存在");
        }

        // 当且仅当 status = 1 合法
        if (appointment.getStatus() == null || appointment.getStatus() != 1) {
            return Result.error("该预约不可支付,当前状态为: " + appointment.getStatus());
        }

        appointment.setStatus(2);
        appointment.setPaymentTime(LocalDateTime.now());
        baseMapper.updateById(appointment);

        MPJLambdaWrapper<Appointment> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.selectAll(Appointment.class)
                .leftJoin(User.class, User::getUserId, Appointment::getPatientUserId)
                .selectAs(User::getUserName, "patientUserName")
                .selectAs(User::getName, "patientName")
                .selectAs(User::getPhone, "patientPhone")
                .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                .selectAs(DoctorSchedule::getScheduleDate, "scheduleDate")
                .selectAs(DoctorSchedule::getSlotType, "slotType")
                .selectAs(DoctorSchedule::getSlotPeriod, "slotPeriod")
                .leftJoin(User.class, "doctor_user", User::getUserId, DoctorSchedule::getDoctorUserId)
                .select("doctor_user.name as doctorName")
                .leftJoin(DoctorProfile.class, DoctorProfile::getUserId, DoctorSchedule::getDoctorUserId)
                .selectAs(DoctorProfile::getTitle, "doctorTitle")
                .selectAs(DoctorProfile::getUserId, "doctorUserId")
                .leftJoin(Department.class, Department::getDepartmentId, DoctorProfile::getDepartmentId)
                .selectAs(Department::getName, "departmentName")
                .eq(Appointment::getAppointmentId, appointmentId);

        AppointmentListDTO dto = baseMapper.selectJoinOne(
                AppointmentListDTO.class,
                queryWrapper
        );



        User user = userService.getById(appointment.getPatientUserId());
        DoctorSchedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
        String userEmail = user.getEmail();

        // 获取时段信息
        String periodDesc = "";
        PeriodEnum periodEnum = PeriodEnum.getEnumByCode(schedule.getSlotPeriod());
        if (periodEnum != null) {
            periodDesc = periodEnum.getDesc();
        }
    
        String subject = "挂号支付成功通知";
        StringBuilder content = new StringBuilder();
        content.append("尊敬的 ").append(user.getName()).append("，您好！\n\n");
        content.append("您的预约已成功支付。\n");
        content.append("预约信息如下：\n");
        content.append("预约号：").append(appointment.getAppointmentNo()).append("\n");
        content.append("就诊日期：").append(schedule.getScheduleDate()).append("\n");
        content.append("就诊时段：").append(periodDesc).append("\n");
        if (dto.getDepartmentName() != null && !dto.getDepartmentName().isEmpty()) {
            content.append("科室：").append(dto.getDepartmentName()).append("\n");
        }
        if (dto.getDoctorName() != null) {
            content.append("医生：").append(dto.getDoctorName()).append("\n");
        }
        content.append("挂号费：¥").append(dto.getActualFee()).append("\n");
        content.append("\n请您准时就诊，如有问题请及时联系医院。\n");
        content.append("\n祝您早日康复！");

        mailService.sendNotification(subject, content.toString(), userEmail);
        log.info("预约支付邮件已发送至: {}", userEmail);


        return Result.success("支付成功", dto);
    }

    @Override
    public Result<AppointmentListDTO> callAppointment(Long appointmentId) {
        Appointment appointment = baseMapper.selectById(appointmentId);
        if (appointment == null) {
            return Result.error("预约记录不存在");
        }

        // 当且仅当 status = 2 合法
        if (appointment.getStatus() == null || appointment.getStatus() != 2) {
            return Result.error("该预约不可支付,当前状态为: " + appointment.getStatus());
        }

        appointment.setStatus(3);
        appointment.setCallingTime(LocalDateTime.now());
        baseMapper.updateById(appointment);

        MPJLambdaWrapper<Appointment> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.selectAll(Appointment.class)
                .leftJoin(User.class, User::getUserId, Appointment::getPatientUserId)
                .selectAs(User::getUserName, "patientUserName")
                .selectAs(User::getName, "patientName")
                .selectAs(User::getPhone, "patientPhone")
                .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                .selectAs(DoctorSchedule::getScheduleDate, "scheduleDate")
                .selectAs(DoctorSchedule::getSlotType, "slotType")
                .selectAs(DoctorSchedule::getSlotPeriod, "slotPeriod")
                .leftJoin(User.class, "doctor_user", User::getUserId, DoctorSchedule::getDoctorUserId)
                .select("doctor_user.name as doctorName")
                .leftJoin(DoctorProfile.class, DoctorProfile::getUserId, DoctorSchedule::getDoctorUserId)
                .selectAs(DoctorProfile::getTitle, "doctorTitle")
                .selectAs(DoctorProfile::getUserId, "doctorUserId")
                .leftJoin(Department.class, Department::getDepartmentId, DoctorProfile::getDepartmentId)
                .selectAs(Department::getName, "departmentName")
                .eq(Appointment::getAppointmentId, appointmentId);

        AppointmentListDTO dto = baseMapper.selectJoinOne(
                AppointmentListDTO.class,
                queryWrapper
        );


        User user = userService.getById(appointment.getPatientUserId());
        DoctorSchedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
        String userEmail = user.getEmail();

        String periodDesc = "";
        PeriodEnum periodEnum = PeriodEnum.getEnumByCode(schedule.getSlotPeriod());
        if (periodEnum != null) {
            periodDesc = periodEnum.getDesc();
        }
    
        String subject = "呼唤就诊通知";
        StringBuilder content = new StringBuilder();
        content.append("尊敬的 ").append(user.getName()).append("，您好！\n\n");
        content.append("现在是您的就诊时间，请前往相应科室就诊。\n");
        content.append("挂号信息如下：\n");
        content.append("预约号：").append(appointment.getAppointmentNo()).append("\n");
        content.append("就诊日期：").append(schedule.getScheduleDate()).append("\n");
        content.append("就诊时段：").append(periodDesc).append("\n");
        if (dto.getDepartmentName() != null && !dto.getDepartmentName().isEmpty()) {
            content.append("科室：").append(dto.getDepartmentName()).append("\n");
        }
        if (dto.getDoctorName() != null) {
            content.append("医生：").append(dto.getDoctorName()).append("\n");
        }
        content.append("挂号费：¥").append(dto.getActualFee()).append("\n");
        content.append("\n感谢您选择我们的医院服务，期待您的下次就诊。\n");
        content.append("\n祝您早日康复！");

        mailService.sendNotification(subject, content.toString(), userEmail);
        log.info("就诊完成邮件已发送至: {}", userEmail);

        

        return Result.success("呼唤成功", dto);
    }

    @Override
    public Result<AppointmentListDTO> completeAppointment(Long appointmentId) {
        Appointment appointment = baseMapper.selectById(appointmentId);
        if (appointment == null) {
            return Result.error("预约记录不存在");
        }

        // 当且仅当 status = 3 合法
        if (appointment.getStatus() == null || appointment.getStatus() != 3) {
            return Result.error("该预约不可支付,当前状态为: " + appointment.getStatus());
        }

        appointment.setStatus(4);
        appointment.setCompletionTime(LocalDateTime.now());
        baseMapper.updateById(appointment);

        MPJLambdaWrapper<Appointment> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.selectAll(Appointment.class)
                .leftJoin(User.class, User::getUserId, Appointment::getPatientUserId)
                .selectAs(User::getUserName, "patientUserName")
                .selectAs(User::getName, "patientName")
                .selectAs(User::getPhone, "patientPhone")
                .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                .selectAs(DoctorSchedule::getScheduleDate, "scheduleDate")
                .selectAs(DoctorSchedule::getSlotType, "slotType")
                .selectAs(DoctorSchedule::getSlotPeriod, "slotPeriod")
                .leftJoin(User.class, "doctor_user", User::getUserId, DoctorSchedule::getDoctorUserId)
                .select("doctor_user.name as doctorName")
                .leftJoin(DoctorProfile.class, DoctorProfile::getUserId, DoctorSchedule::getDoctorUserId)
                .selectAs(DoctorProfile::getTitle, "doctorTitle")
                .selectAs(DoctorProfile::getUserId, "doctorUserId")
                .leftJoin(Department.class, Department::getDepartmentId, DoctorProfile::getDepartmentId)
                .selectAs(Department::getName, "departmentName")
                .eq(Appointment::getAppointmentId, appointmentId);

        AppointmentListDTO dto = baseMapper.selectJoinOne(
                AppointmentListDTO.class,
                queryWrapper
        );



        User user = userService.getById(appointment.getPatientUserId());
        DoctorSchedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
        String userEmail = user.getEmail();

        // 获取时段信息
        String periodDesc = "";
        PeriodEnum periodEnum = PeriodEnum.getEnumByCode(schedule.getSlotPeriod());
        if (periodEnum != null) {
            periodDesc = periodEnum.getDesc();
        }
    
        String subject = "就诊完成通知";
        StringBuilder content = new StringBuilder();
        content.append("尊敬的 ").append(user.getName()).append("，您好！\n\n");
        content.append("您的就诊已完成。\n");
        content.append("挂号信息如下：\n");
        content.append("预约号：").append(appointment.getAppointmentNo()).append("\n");
        content.append("就诊日期：").append(schedule.getScheduleDate()).append("\n");
        content.append("就诊时段：").append(periodDesc).append("\n");
        if (dto.getDepartmentName() != null && !dto.getDepartmentName().isEmpty()) {
            content.append("科室：").append(dto.getDepartmentName()).append("\n");
        }
        if (dto.getDoctorName() != null) {
            content.append("医生：").append(dto.getDoctorName()).append("\n");
        }
        content.append("挂号费：¥").append(dto.getActualFee()).append("\n");
        content.append("\n感谢您选择我们的医院服务，期待您的下次就诊。\n");
        content.append("\n祝您早日康复！");

        mailService.sendNotification(subject, content.toString(), userEmail);
        log.info("就诊完成邮件已发送至: {}", userEmail);


        return Result.success("完成成功", dto);
    }

    @Override
    public Result<AppointmentListDTO> noShowAppointment(Long appointmentId) {
        Appointment appointment = baseMapper.selectById(appointmentId);
        if (appointment == null) {
            return Result.error("预约记录不存在");
        }

        // 允许状态为2(已预约)或3(传呼)的预约标记为未到
        if (appointment.getStatus() == null || (appointment.getStatus() != 2 && appointment.getStatus() != 3)) {
            return Result.error("该预约不可标记为未到,当前状态为: " + appointment.getStatus());
        }

        appointment.setStatus(6);
        baseMapper.updateById(appointment);

        MPJLambdaWrapper<Appointment> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.selectAll(Appointment.class)
                .leftJoin(User.class, User::getUserId, Appointment::getPatientUserId)
                .selectAs(User::getUserName, "patientUserName")
                .selectAs(User::getName, "patientName")
                .selectAs(User::getPhone, "patientPhone")
                .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                .selectAs(DoctorSchedule::getScheduleDate, "scheduleDate")
                .selectAs(DoctorSchedule::getSlotType, "slotType")
                .selectAs(DoctorSchedule::getSlotPeriod, "slotPeriod")
                .leftJoin(User.class, "doctor_user", User::getUserId, DoctorSchedule::getDoctorUserId)
                .select("doctor_user.name as doctorName")
                .leftJoin(DoctorProfile.class, DoctorProfile::getUserId, DoctorSchedule::getDoctorUserId)
                .selectAs(DoctorProfile::getTitle, "doctorTitle")
                .selectAs(DoctorProfile::getUserId, "doctorUserId")
                .leftJoin(Department.class, Department::getDepartmentId, DoctorProfile::getDepartmentId)
                .selectAs(Department::getName, "departmentName")
                .eq(Appointment::getAppointmentId, appointmentId);

        AppointmentListDTO dto = baseMapper.selectJoinOne(
                AppointmentListDTO.class,
                queryWrapper
        );
        



        User user = userService.getById(appointment.getPatientUserId());
        DoctorSchedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
        String userEmail = user.getEmail();

        // 获取时段信息
        String periodDesc = "";
        PeriodEnum periodEnum = PeriodEnum.getEnumByCode(schedule.getSlotPeriod());
        if (periodEnum != null) {
            periodDesc = periodEnum.getDesc();
        }
    
        String subject = "挂号未到通知";
        StringBuilder content = new StringBuilder();
        content.append("尊敬的 ").append(user.getName()).append("，您好！\n\n");
        content.append("您的预约未到诊。\n");
        content.append("预约信息如下：\n");
        content.append("预约号：").append(appointment.getAppointmentNo()).append("\n");
        content.append("就诊日期：").append(schedule.getScheduleDate()).append("\n");
        content.append("就诊时段：").append(periodDesc).append("\n");
        if (dto.getDepartmentName() != null && !dto.getDepartmentName().isEmpty()) {
            content.append("科室：").append(dto.getDepartmentName()).append("\n");
        }
        if (dto.getDoctorName() != null) {
            content.append("医生：").append(dto.getDoctorName()).append("\n");
        }
        content.append("挂号费：¥").append(dto.getActualFee()).append("\n");
        content.append("\n根据医院规定，未到诊的预约将视为放弃挂号资格。且此次挂号费用不予退还。\n");

        mailService.sendNotification(subject, content.toString(), userEmail);
        log.info("预约支付邮件已发送至: {}", userEmail);


        return Result.success("取缔成功", dto);
    }

    @Override
    public Result<AppointmentDTO.AppointmentCheckInTokenDTO> generateCheckInToken() {
        String token = UUID.randomUUID().toString();
        String key = RedisEnum.CHECK_IN_TOKEN.getDesc() + token;
        redisUtil.setCacheObject(key, "1", (int) CHECK_IN_TOKEN_TTL_MINUTES, TimeUnit.MINUTES);

        AppointmentDTO.AppointmentCheckInTokenDTO dto = new AppointmentDTO.AppointmentCheckInTokenDTO();
        dto.setToken(token);
        return Result.success("生成签到token成功", dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<AppointmentListDTO> checkInAppointment(Long appointmentId, String token) {
        if (token == null || token.trim().isEmpty()) {
            return Result.error("签到token不能为空");
        }

        String redisKey = RedisEnum.CHECK_IN_TOKEN.getDesc() + token;
        Object cachedToken = redisUtil.getCacheObject(redisKey);
        if (cachedToken == null) {
            return Result.error("签到token无效或已过期");
        }

        Appointment appointment = baseMapper.selectById(appointmentId);
        if (appointment == null) {
            return Result.error("预约记录不存在");
        }

        if (appointment.getCheckInTime() != null) {
            return Result.error("该预约已签到");
        }

        if (appointment.getStatus() == null || (appointment.getStatus() != 2 && appointment.getStatus() != 3)) {
            return Result.error("当前预约状态不可签到");
        }

        appointment.setCheckInTime(LocalDateTime.now());
        baseMapper.updateById(appointment);

        // token设置为一次性使用，签到成功后删除
        redisUtil.deleteObject(redisKey);

        MPJLambdaWrapper<Appointment> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.selectAll(Appointment.class)
                .leftJoin(User.class, User::getUserId, Appointment::getPatientUserId)
                .selectAs(User::getUserName, "patientUserName")
                .selectAs(User::getName, "patientName")
                .selectAs(User::getPhone, "patientPhone")
                .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                .selectAs(DoctorSchedule::getScheduleDate, "scheduleDate")
                .selectAs(DoctorSchedule::getSlotType, "slotType")
                .selectAs(DoctorSchedule::getSlotPeriod, "slotPeriod")
                .leftJoin(User.class, "doctor_user", User::getUserId, DoctorSchedule::getDoctorUserId)
                .select("doctor_user.name as doctorName")
                .leftJoin(DoctorProfile.class, DoctorProfile::getUserId, DoctorSchedule::getDoctorUserId)
                .selectAs(DoctorProfile::getTitle, "doctorTitle")
                .selectAs(DoctorProfile::getUserId, "doctorUserId")
                .leftJoin(Department.class, Department::getDepartmentId, DoctorProfile::getDepartmentId)
                .selectAs(Department::getName, "departmentName")
                .eq(Appointment::getAppointmentId, appointmentId);
        
        AppointmentListDTO dto = baseMapper.selectJoinOne(AppointmentListDTO.class, queryWrapper);
        return Result.success("签到成功", dto);
    }

    /**
     * every min
     * 定时检测逾期未签到的预约
     */
    @Scheduled(cron = "0 * * * * ?") // evert min 第0秒执行
    @Transactional(rollbackFor = Exception.class)
    public void checkOverdueAppointments() {
        log.info("开始执行逾期预约检测任务");

        try {

            List<Appointment> appointments = lambdaQuery()
                .in(Appointment::getStatus, 2, 3)
                .eq(Appointment::getIsDeleted, 0)
                .isNull(Appointment::getCheckInTime)
                .list();

            if (appointments.isEmpty()) {
                log.info("当前没有需要检测的预约");
                return;
            }

            log.info("找到 {} 条待检测的预约记录", appointments.size());

            int overdueCount = 0;
            LocalDateTime now = LocalDateTime.now();

            for (Appointment appointment : appointments) {

                DoctorSchedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
                if (schedule == null) {
                    log.warn("预约ID {} 的排班信息不存在，跳过", appointment.getAppointmentId());
                    continue;
                }


                PeriodEnum periodEnum = PeriodEnum.getEnumByCode(schedule.getSlotPeriod());
                if (periodEnum == null) {
                    log.warn("预约ID {} 的时段信息无效，跳过", appointment.getAppointmentId());
                    continue;
                }
                String timeRange = periodEnum.getDesc();
                String endTimeStr = timeRange.split("-")[1];
                LocalTime endTime = LocalTime.parse(endTimeStr);


                LocalDateTime appointmentEndTime = LocalDateTime.of(schedule.getScheduleDate(), endTime);


                LocalDateTime overdueTime = appointmentEndTime.plusMinutes(OVERDUE_DELAY_MINUTES);

                // 检查是否已逾期且未签到
                if (now.isAfter(overdueTime) && appointment.getCheckInTime() == null) {
                    log.info("预约ID {} 已逾期，预约时间: {}, 逾期检测时间: {}, 当前时间: {}, 签到时间: null",
                            appointment.getAppointmentId(),
                            appointmentEndTime,
                            overdueTime,
                            now);


                    Result<AppointmentListDTO> result = noShowAppointment(appointment.getAppointmentId());

                    if (result.getCode() == 1) {
                        overdueCount++;
                        log.info("预约ID {} 已自动标记为未到", appointment.getAppointmentId());
                    } else {
                        log.error("预约ID {} 标记为未到失败: {}", appointment.getAppointmentId(), result.getMsg());
                    }
                }
            }

            log.info("逾期预约检测任务完成，共处理 {} 条逾期记录", overdueCount);

        } catch (Exception e) {
            log.error("逾期预约检测任务执行失败", e);
        }
    }

}
