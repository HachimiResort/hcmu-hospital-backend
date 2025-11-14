package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;

import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.OpRuleEnum;
import org.hcmu.hcmucommon.enumeration.PeriodEnum;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.AppointmentDTO;
import org.hcmu.hcmupojo.dto.AppointmentDTO.AppointmentListDTO;
import org.hcmu.hcmupojo.dto.OperationRuleDTO.RuleInfo;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.entity.*;
import org.hcmu.hcmuserver.mapper.appointment.AppointmentMapper;
import org.hcmu.hcmuserver.mapper.schedule.ScheduleMapper;
import org.hcmu.hcmuserver.service.AppointmentService;
import org.hcmu.hcmuserver.service.OperationRuleService;
import org.hcmu.hcmuserver.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@Slf4j
public class AppointmentServiceImpl extends MPJBaseServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    @Autowired
    private UserService userService;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private OperationRuleService operationRuleService;

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
                .leftJoin(Schedule.class, Schedule::getScheduleId, Appointment::getScheduleId)
                .selectAs(Schedule::getScheduleDate, "scheduleDate")
                .selectAs(Schedule::getSlotType, "slotType")
                .selectAs(Schedule::getSlotPeriod, "slotPeriod")
                // 关联医生用户表（通过排班表的doctor_user_id）
                .leftJoin(User.class, "doctor_user", User::getUserId, Schedule::getDoctorUserId)
                .select("doctor_user.name as doctorName")
                // 关联医生档案表（获取职称）
                .leftJoin(DoctorProfile.class, DoctorProfile::getUserId, Schedule::getDoctorUserId)
                .selectAs(DoctorProfile::getTitle, "doctorTitle")
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

        // 默认查询未删除的记录
        if (requestDTO.getIsDeleted() == null) {
            queryWrapper.eq(Appointment::getIsDeleted, 0);
        }

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
                .leftJoin(Schedule.class, Schedule::getScheduleId, Appointment::getScheduleId)
                .selectAs(Schedule::getScheduleDate, "scheduleDate")
                .selectAs(Schedule::getSlotType, "slotType")
                .selectAs(Schedule::getSlotPeriod, "slotPeriod")
                // 关联医生用户表（通过排班表的doctor_user_id）
                .leftJoin(User.class, "doctor_user", User::getUserId, Schedule::getDoctorUserId)
                .select("doctor_user.name as doctorName")
                // 关联医生档案表（获取职称）
                .leftJoin(DoctorProfile.class, DoctorProfile::getUserId, Schedule::getDoctorUserId)
                .selectAs(DoctorProfile::getTitle, "doctorTitle")
                // 关联科室表（通过医生档案表的department_id）
                .leftJoin(Department.class, Department::getDepartmentId, DoctorProfile::getDepartmentId)
                .selectAs(Department::getName, "departmentName")
                .eq(Appointment::getAppointmentId, appointmentId)
                .eq(Appointment::getIsDeleted, 0);  // 只查询未删除的记录

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
                .eq(Appointment::getIsDeleted, 0)
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

        Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
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

        // 恢复号源
        schedule.setAvailableSlots(schedule.getAvailableSlots() + 1);
        scheduleMapper.updateById(schedule);

        // 查询完整的预约信息（包含关联的患者、排班、医生、科室信息）
        MPJLambdaWrapper<Appointment> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.selectAll(Appointment.class)

                .leftJoin(User.class, User::getUserId, Appointment::getPatientUserId)
                .selectAs(User::getUserName, "patientUserName")
                .selectAs(User::getName, "patientName")
                .selectAs(User::getPhone, "patientPhone")
                .leftJoin(Schedule.class, Schedule::getScheduleId, Appointment::getScheduleId)
                .selectAs(Schedule::getScheduleDate, "scheduleDate")
                .selectAs(Schedule::getSlotType, "slotType")
                .selectAs(Schedule::getSlotPeriod, "slotPeriod")
                .leftJoin(User.class, "doctor_user", User::getUserId, Schedule::getDoctorUserId)
                .select("doctor_user.name as doctorName")
                .leftJoin(DoctorProfile.class, DoctorProfile::getUserId, Schedule::getDoctorUserId)
                .selectAs(DoctorProfile::getTitle, "doctorTitle")
                .leftJoin(Department.class, Department::getDepartmentId, DoctorProfile::getDepartmentId)
                .selectAs(Department::getName, "departmentName")
                .eq(Appointment::getAppointmentId, appointmentId);

        AppointmentListDTO dto = baseMapper.selectJoinOne(
                AppointmentListDTO.class,
                queryWrapper
        );

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
                .leftJoin(Schedule.class, Schedule::getScheduleId, Appointment::getScheduleId)
                .selectAs(Schedule::getScheduleDate, "scheduleDate")
                .selectAs(Schedule::getSlotType, "slotType")
                .selectAs(Schedule::getSlotPeriod, "slotPeriod")
                .leftJoin(User.class, "doctor_user", User::getUserId, Schedule::getDoctorUserId)
                .select("doctor_user.name as doctorName")
                .leftJoin(DoctorProfile.class, DoctorProfile::getUserId, Schedule::getDoctorUserId)
                .selectAs(DoctorProfile::getTitle, "doctorTitle")
                .leftJoin(Department.class, Department::getDepartmentId, DoctorProfile::getDepartmentId)
                .selectAs(Department::getName, "departmentName")
                .eq(Appointment::getAppointmentId, appointmentId);

        AppointmentListDTO dto = baseMapper.selectJoinOne(
                AppointmentListDTO.class,
                queryWrapper
        );

        return Result.success("支付成功", dto);
    }



    
}
