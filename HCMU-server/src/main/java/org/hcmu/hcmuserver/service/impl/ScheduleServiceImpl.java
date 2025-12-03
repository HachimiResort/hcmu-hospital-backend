package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;

import org.hcmu.hcmucommon.enumeration.PeriodEnum;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmucommon.enumeration.RoleTypeEnum;
import org.hcmu.hcmucommon.enumeration.OpRuleEnum;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.ScheduleDTO;
import org.hcmu.hcmupojo.dto.AppointmentDTO;
import org.hcmu.hcmupojo.dto.OperationRuleDTO.RuleInfo;
import org.hcmu.hcmupojo.entity.Appointment;
import org.hcmu.hcmupojo.entity.DoctorSchedule;
import org.hcmu.hcmupojo.entity.Role;
import org.hcmu.hcmupojo.entity.User;
import org.hcmu.hcmupojo.entity.Department;
import org.hcmu.hcmupojo.entity.DoctorProfile;
import org.hcmu.hcmupojo.LoginUser;
import org.hcmu.hcmupojo.entity.relation.UserRole;
import org.hcmu.hcmuserver.mapper.schedule.ScheduleMapper;
import org.hcmu.hcmuserver.mapper.appointment.AppointmentMapper;
import org.hcmu.hcmuserver.mapper.user.UserMapper;
import org.hcmu.hcmuserver.mapper.user.UserRoleMapper;
import org.hcmu.hcmuserver.mapper.department.DepartmentMapper;
import org.hcmu.hcmuserver.mapper.doctorprofile.DoctorProfileMapper;
import org.hcmu.hcmuserver.service.ScheduleService;
import org.hcmu.hcmuserver.service.UserService;
import org.hcmu.hcmuserver.service.OperationRuleService;
import org.hcmu.hcmuserver.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ScheduleServiceImpl extends MPJBaseServiceImpl<ScheduleMapper, DoctorSchedule> implements ScheduleService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private OperationRuleService operationRuleService;

    @Autowired
    private DoctorProfileMapper doctorProfileMapper;

    @Autowired
    private org.hcmu.hcmuserver.mapper.patientprofile.PatientProfileMapper patientProfileMapper;

    @Autowired
    private MailService mailService;

    @Override
    public Result<ScheduleDTO.ScheduleListDTO> createSchedule(ScheduleDTO.ScheduleCreateDTO createDTO) {

        // 校验用户是否存在
        User user = userService.getById(createDTO.getDoctorUserId());
        if (user == null) {
            return Result.error("用户不存在");
        }

        Long userId = createDTO.getDoctorUserId();

        // 是否为医生角色呢
        MPJLambdaWrapper<UserRole> roleQueryWrapper = new MPJLambdaWrapper<>();
        roleQueryWrapper.select(Role::getType)
                .leftJoin(Role.class, Role::getRoleId, UserRole::getRoleId)
                .eq(UserRole::getUserId, userId);

        Role userRole = userRoleMapper.selectJoinOne(Role.class, roleQueryWrapper);
        if (userRole == null) {
            return Result.error("用户未分配角色");
        }

        if (!RoleTypeEnum.DOCTOR.getCode().equals(userRole.getType())) {
            return Result.error("用户不是医生角色，无法创建排班");
        }


        // 校验同一医生在同一日期和时段是否已有排班
        LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorSchedule::getDoctorUserId, createDTO.getDoctorUserId())
                .eq(DoctorSchedule::getScheduleDate, createDTO.getScheduleDate())
                .eq(DoctorSchedule::getSlotPeriod, createDTO.getSlotPeriod())
                .eq(DoctorSchedule::getStatus, 1);
        if (baseMapper.selectCount(wrapper) > 0) {
            return Result.error("该医生在此日期和时段已有排班");
        
        }

        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDoctorUserId(createDTO.getDoctorUserId());
        schedule.setScheduleDate(createDTO.getScheduleDate());
        schedule.setSlotType(createDTO.getSlotType());
        schedule.setTotalSlots(createDTO.getTotalSlots());
        schedule.setAvailableSlots(createDTO.getTotalSlots());
        schedule.setFee(createDTO.getFee());
        schedule.setStatus(createDTO.getStatus());
        schedule.setSlotPeriod(createDTO.getSlotPeriod());
        schedule.setIsDeleted(0);
        schedule.setCreateTime(LocalDateTime.now());
        schedule.setUpdateTime(LocalDateTime.now());

        baseMapper.insert(schedule);

        ScheduleDTO.ScheduleListDTO result = new ScheduleDTO.ScheduleListDTO();
        result.setScheduleId(schedule.getScheduleId());
        result.setDoctorUserId(schedule.getDoctorUserId());
        result.setScheduleDate(schedule.getScheduleDate());
        result.setSlotType(schedule.getSlotType());
        result.setTotalSlots(schedule.getTotalSlots());
        result.setAvailableSlots(schedule.getAvailableSlots());
        result.setFee(schedule.getFee());
        result.setStatus(schedule.getStatus());
        result.setSlotPeriod(schedule.getSlotPeriod());
        result.setCreateTime(schedule.getCreateTime());

        return Result.success(result);
    }

    @Override
    public Result<PageDTO<ScheduleDTO.ScheduleListDTO>> findAllSchedules(ScheduleDTO.ScheduleGetRequestDTO requestDTO) {
        MPJLambdaWrapper<DoctorSchedule> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(DoctorSchedule::getScheduleId, DoctorSchedule::getDoctorUserId,
                        DoctorSchedule::getScheduleDate, DoctorSchedule::getSlotType, DoctorSchedule::getSlotPeriod,
                        DoctorSchedule::getTotalSlots, DoctorSchedule::getAvailableSlots, DoctorSchedule::getFee,
                        DoctorSchedule::getStatus, DoctorSchedule::getCreateTime)
                .eq(requestDTO.getDoctorUserId() != null, DoctorSchedule::getDoctorUserId, requestDTO.getDoctorUserId())
                .ge(requestDTO.getScheduleStartDate() != null, DoctorSchedule::getScheduleDate, requestDTO.getScheduleStartDate()) 
                .le(requestDTO.getScheduleEndDate() != null, DoctorSchedule::getScheduleDate, requestDTO.getScheduleEndDate())  
                .eq(requestDTO.getSlotType() != null, DoctorSchedule::getSlotType, requestDTO.getSlotType())
                .eq(requestDTO.getSlotPeriod() != null, DoctorSchedule::getSlotPeriod, requestDTO.getSlotPeriod())
                .eq(requestDTO.getStatus() != null, DoctorSchedule::getStatus, requestDTO.getStatus())
                .orderByAsc(DoctorSchedule::getSlotType);

        IPage<ScheduleDTO.ScheduleListDTO> page = baseMapper.selectJoinPage(
                new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize()),
                ScheduleDTO.ScheduleListDTO.class,
                queryWrapper);

        return Result.success(new PageDTO<>(page));
    }

    @Override
    public Result<ScheduleDTO.ScheduleListDTO> findScheduleById(Long scheduleId) {
        DoctorSchedule schedule = baseMapper.selectById(scheduleId);
        if (schedule == null || schedule.getIsDeleted() == 1) {
            return Result.error("排班不存在");
        }

        ScheduleDTO.ScheduleListDTO dto = new ScheduleDTO.ScheduleListDTO();
        dto.setScheduleId(schedule.getScheduleId());
        dto.setDoctorUserId(schedule.getDoctorUserId());
        dto.setScheduleDate(schedule.getScheduleDate());
        dto.setSlotType(schedule.getSlotType());
        dto.setSlotPeriod(schedule.getSlotPeriod());
        dto.setTotalSlots(schedule.getTotalSlots());
        dto.setAvailableSlots(schedule.getAvailableSlots());
        dto.setFee(schedule.getFee());
        dto.setStatus(schedule.getStatus());
        dto.setCreateTime(schedule.getCreateTime());

        return Result.success(dto);
    }

    @Override
    public Result<String> updateScheduleById(Long scheduleId, ScheduleDTO.ScheduleUpdateDTO updateDTO) {
        DoctorSchedule schedule = baseMapper.selectById(scheduleId);
        if (schedule == null || schedule.getIsDeleted() == 1) {
            return Result.error("排班不存在");
        }

        if (updateDTO.getDoctorUserId() != null && !updateDTO.getDoctorUserId().equals(schedule.getDoctorUserId())) {
            // 检查用户是否存在
            User user = userMapper.selectById(updateDTO.getDoctorUserId());
            if (user == null || user.getIsDeleted() == 1) {
                return Result.error("指定的医生用户不存在");
            }

            // 检查用户是否为医生角色
            MPJLambdaWrapper<UserRole> roleQueryWrapper = new MPJLambdaWrapper<>();
            roleQueryWrapper.select(Role::getType)
                    .leftJoin(Role.class, Role::getRoleId, UserRole::getRoleId)
                    .eq(UserRole::getUserId, updateDTO.getDoctorUserId());

            Role userRole = userRoleMapper.selectJoinOne(Role.class, roleQueryWrapper);
            if (userRole == null) {
                return Result.error("用户未分配角色");
            }

            if (!RoleTypeEnum.DOCTOR.getCode().equals(userRole.getType())) {
                return Result.error("指定的用户不是医生角色，无法分配排班");
            }
        }

        if ((updateDTO.getDoctorUserId() != null && !updateDTO.getDoctorUserId().equals(schedule.getDoctorUserId())) ||
            (updateDTO.getScheduleDate() != null && !updateDTO.getScheduleDate().equals(schedule.getScheduleDate())) ||
            (updateDTO.getSlotType() != null && !updateDTO.getSlotType().equals(schedule.getSlotType()))) {
            
            Long doctorUserId = updateDTO.getDoctorUserId() != null ? updateDTO.getDoctorUserId() : schedule.getDoctorUserId();
            java.time.LocalDate scheduleDate = updateDTO.getScheduleDate() != null ? updateDTO.getScheduleDate() : schedule.getScheduleDate();
            Integer slotType = updateDTO.getSlotType() != null ? updateDTO.getSlotType() : schedule.getSlotType();
            
            LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DoctorSchedule::getDoctorUserId, doctorUserId)
                    .eq(DoctorSchedule::getScheduleDate, scheduleDate)
                    .eq(DoctorSchedule::getSlotType, slotType)
                    .eq(DoctorSchedule::getStatus, 1)
                    .ne(DoctorSchedule::getScheduleId, scheduleId); // 排除当前记录
            if (baseMapper.selectCount(wrapper) > 0) {
                return Result.error("该医生在此日期和时段已有排班");
            }
        }


        Integer totalSlots = updateDTO.getTotalSlots() != null ? updateDTO.getTotalSlots() : schedule.getTotalSlots();
        Integer availableSlots = updateDTO.getAvailableSlots() != null ? updateDTO.getAvailableSlots() : schedule.getAvailableSlots();
        
        if (totalSlots != null && availableSlots != null && availableSlots > totalSlots) {
            return Result.error("可用号源数不能大于总号源数");
        }

        updateDTO.updateSchedule(schedule);
        baseMapper.updateById(schedule);
        return Result.success("更新成功");
    }

    @Override
    public Result<String> deleteScheduleById(Long scheduleId) {
        DoctorSchedule schedule = baseMapper.selectById(scheduleId);
        if (schedule == null) {
            return Result.error("排班不存在");
        }


        LambdaQueryWrapper<Appointment> appointmentWrapper = new LambdaQueryWrapper<>();
        appointmentWrapper.eq(Appointment::getScheduleId, scheduleId)
                .in(Appointment::getStatus, 1, 2, 3);
        Long appointmentCount = appointmentMapper.selectCount(appointmentWrapper);

        if (appointmentCount > 0) {
            return Result.error("该排班存在未完成的预约，无法删除");
        }


        baseMapper.deleteById(scheduleId);
        return Result.success("删除成功");
    }

    @Override
    public Result<String> batchDeleteSchedules(List<Long> scheduleIds) {
        if (CollectionUtils.isEmpty(scheduleIds)) {
            return Result.error("请选择需要删除的排班");
        }

        // 校验排班是否存在
        LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DoctorSchedule::getScheduleId, scheduleIds);
        long existCount = baseMapper.selectCount(wrapper);
        if (existCount != scheduleIds.size()) {
            return Result.error("部分排班不存在");
        }

        LambdaQueryWrapper<Appointment> appointmentWrapper = new LambdaQueryWrapper<>();
        appointmentWrapper.in(Appointment::getScheduleId, scheduleIds)
                .in(Appointment::getStatus, 1, 2, 3);
        Long appointmentCount = appointmentMapper.selectCount(appointmentWrapper);

        if (appointmentCount > 0) {
            return Result.error("部分排班存在未完成的预约，无法删除");
        }


        baseMapper.deleteBatchIds(scheduleIds);

        return Result.success("批量删除成功");
    }

    @Override
    public Result<String> copySchedule(ScheduleDTO.ScheduleCopyDTO copyDTO) {
        Long doctorUserId = copyDTO.getDoctorUserId();
        LocalDate targetDate = copyDTO.getTargetDate();

        // 校验用户是否存在
        User user = userService.getById(copyDTO.getDoctorUserId());
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 是否为医生角色
        MPJLambdaWrapper<UserRole> roleQueryWrapper = new MPJLambdaWrapper<>();
        roleQueryWrapper.select(Role::getType)
                .leftJoin(Role.class, Role::getRoleId, UserRole::getRoleId)
                .eq(UserRole::getUserId, doctorUserId)
                .eq(UserRole::getIsDeleted, 0)
                .eq(Role::getIsDeleted, 0);

        Role userRole = userRoleMapper.selectJoinOne(Role.class, roleQueryWrapper);
        if (userRole == null) {
            return Result.error("用户未分配角色");
        }

        if (!RoleTypeEnum.DOCTOR.getCode().equals(userRole.getType())) {
            return Result.error("用户不是医生角色，无法复制排班");
        }

        // 检查是否已有排班
        LambdaQueryWrapper<DoctorSchedule> targetDateWrapper = new LambdaQueryWrapper<>();
        targetDateWrapper.eq(DoctorSchedule::getDoctorUserId, doctorUserId)
                .eq(DoctorSchedule::getScheduleDate, targetDate)
                .eq(DoctorSchedule::getStatus, 1);
        
        Long existingScheduleCount = baseMapper.selectCount(targetDateWrapper);
        if (existingScheduleCount > 0) {
            return Result.error("目标日期已有排班，无法复制");
        }

        // 查找7天前的排班
        LocalDate sourceDate = targetDate.minusDays(7);
        LambdaQueryWrapper<DoctorSchedule> sourceDateWrapper = new LambdaQueryWrapper<>();
        sourceDateWrapper.eq(DoctorSchedule::getDoctorUserId, doctorUserId)
                .eq(DoctorSchedule::getScheduleDate, sourceDate)
                .eq(DoctorSchedule::getIsDeleted, 0)
                .eq(DoctorSchedule::getStatus, 1);
        
        List<DoctorSchedule> sourceSchedules = baseMapper.selectList(sourceDateWrapper);
        if (sourceSchedules.isEmpty()) {
            return Result.error("7天前没有排班记录");
        }

        // 4. 复制排班记录
        for (DoctorSchedule sourceSchedule : sourceSchedules) {
            DoctorSchedule newSchedule = new DoctorSchedule();
            newSchedule.setDoctorUserId(sourceSchedule.getDoctorUserId());
            newSchedule.setScheduleDate(targetDate);
            newSchedule.setSlotType(sourceSchedule.getSlotType());
            newSchedule.setSlotPeriod(sourceSchedule.getSlotPeriod());
            newSchedule.setTotalSlots(sourceSchedule.getTotalSlots());
            newSchedule.setAvailableSlots(sourceSchedule.getTotalSlots()); 
            newSchedule.setFee(sourceSchedule.getFee());
            newSchedule.setStatus(sourceSchedule.getStatus());
            newSchedule.setIsDeleted(0);
            newSchedule.setCreateTime(LocalDateTime.now());
            newSchedule.setUpdateTime(LocalDateTime.now());
            
            baseMapper.insert(newSchedule);
        }

        return Result.success("成功复制 " + sourceSchedules.size() + " 条排班记录到 " + targetDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<AppointmentDTO.AppointmentListDTO> appointSchedule(Long scheduleId) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long patientUserId = loginUser.getUser().getUserId();
        DoctorSchedule schedule = baseMapper.selectById(scheduleId);
        if (schedule.getStatus() != null && !Integer.valueOf(1).equals(schedule.getStatus())) {
            return Result.error("当前排班暂不可预约");
        }

        // 提前天数限制
        RuleInfo futureRuleInfo = operationRuleService.getRuleValueByCode(OpRuleEnum.BOOKING_MAX_FUTURE_DAYS);
        if (futureRuleInfo != null && futureRuleInfo.getEnabled() == 1) {
            Integer maxFutureDays = futureRuleInfo.getValue();
            LocalDate maxFutureDate = LocalDate.now().plusDays(maxFutureDays);

            if (schedule.getScheduleDate().isAfter(maxFutureDate)) {
                return Result.error("该排班日期超过可预约的最大提前天数（" + maxFutureDays + "天）");
            }

            log.info("检查排班日期 {} 是否在允许范围内，最大可预约日期: {}",
                     schedule.getScheduleDate(), maxFutureDate);
        }

        // 就诊前多少小时停止预约
        RuleInfo minHoursRule = operationRuleService.getRuleValueByCode(OpRuleEnum.BOOKING_MIN_HOURS_BEFORE_BOOKING_END);
        if (minHoursRule != null && minHoursRule.getEnabled() == 1) {
            Integer minHours = minHoursRule.getValue();
            PeriodEnum periodEnum = PeriodEnum.getEnumByCode(schedule.getSlotPeriod());
            if (periodEnum != null) {
                String startTimeStr = periodEnum.getDesc().split("-")[0];
                LocalTime startTime = LocalTime.parse(startTimeStr);

                LocalDateTime scheduleDateTime = LocalDateTime.of(schedule.getScheduleDate(), startTime);
                log.info("当前时间: {}, 排班时间: {}, 预约截止时间: {}", LocalDateTime.now(), scheduleDateTime, scheduleDateTime.minusHours(minHours));
                LocalDateTime bookingEndTime = scheduleDateTime.minusHours(minHours);

                if (LocalDateTime.now().isAfter(bookingEndTime)) {
                    return Result.error("该排班已停止预约，请在就诊开始前 " + minHours + " 小时完成预约");
                }
            }
        }


        Integer availableSlots = schedule.getAvailableSlots();
        if (availableSlots == null || availableSlots <= 0) {
            return Result.error("该排班号源已满");
        }

        // 校验患者角色
        MPJLambdaWrapper<UserRole> roleQueryWrapper = new MPJLambdaWrapper<>();
        roleQueryWrapper.select(Role::getType)
                .leftJoin(Role.class, Role::getRoleId, UserRole::getRoleId)
                .eq(UserRole::getUserId, patientUserId);

        Role userRole = userRoleMapper.selectJoinOne(Role.class, roleQueryWrapper);
        if (userRole == null || !RoleTypeEnum.PATIENT.getCode().equals(userRole.getType())) {
            return Result.error("当前用户不是患者角色，无法预约");
        }

        // 爽约限制挂号检查
        RuleInfo noShowPunishRule = operationRuleService.getRuleValueByCode(OpRuleEnum.CANCEL_NO_SHOW_PUNISH_DAYS);
        RuleInfo noShowCountThresholdRule = operationRuleService.getRuleValueByCode(OpRuleEnum.CANCEL_NO_SHOW_LIMIT);

        if (noShowPunishRule != null && noShowPunishRule.getEnabled() == 1
            && noShowCountThresholdRule != null && noShowCountThresholdRule.getEnabled() == 1) {
            Integer punishDays = noShowPunishRule.getValue();
            Integer noShowCountThreshold = noShowCountThresholdRule.getValue();

            LocalDate currentDate = LocalDate.now();
            LocalDate startDate = currentDate.minusDays(90); // 查询最近90天内的爽约记录

            // 查询最近90天内的爽约记录
            MPJLambdaWrapper<Appointment> noShowWrapper = new MPJLambdaWrapper<>();
            noShowWrapper
                .selectAll(Appointment.class)
                .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                .eq(Appointment::getPatientUserId, patientUserId)
                .eq(Appointment::getStatus, 6)
                .eq(Appointment::getIsDeleted, 0)
                .eq(DoctorSchedule::getStatus, 1)
                .ge(DoctorSchedule::getScheduleDate, startDate)
                .orderByDesc(DoctorSchedule::getScheduleDate);

            List<Appointment> noShowAppointments = appointmentMapper.selectJoinList(Appointment.class, noShowWrapper);

            // 爽约是否达到阈值
            if (noShowAppointments.size() >= noShowCountThreshold) {

                Appointment lastNoShowAppointment = noShowAppointments.get(0);
                DoctorSchedule lastNoShowSchedule = baseMapper.selectById(lastNoShowAppointment.getScheduleId());

                if (lastNoShowSchedule != null) {
                    LocalDate lastNoShowDate = lastNoShowSchedule.getScheduleDate();
                    LocalDate punishmentEndDate = lastNoShowDate.plusDays(punishDays);

                    // 当前日期在惩罚期内
                    if (currentDate.isBefore(punishmentEndDate)) {
                        log.info("用户ID {} 因爽约被限制挂号，90天内爽约次数: {}, 最后爽约日期: {}, 惩罚结束日期: {}",
                                 patientUserId, noShowAppointments.size(), lastNoShowDate, punishmentEndDate);
                        return Result.error("您因爽约记录被限制挂号(90天内爽约" + noShowAppointments.size() + "次)，限制至 " + punishmentEndDate + "，请届时再试");
                    }
                }
            }
        }

        // 防止重复预约
        LambdaQueryWrapper<Appointment> duplicateWrapper = new LambdaQueryWrapper<>();
        duplicateWrapper.eq(Appointment::getScheduleId, scheduleId)
                .eq(Appointment::getPatientUserId, patientUserId)
                .ne(Appointment::getStatus, 5)
                .ne(Appointment::getStatus, 6);
        if (appointmentMapper.selectCount(duplicateWrapper) > 0) {
            return Result.error("请勿重复预约该排班");
        }

        // 是否能同一时段多重预约限制
        RuleInfo sameTimeSlotRule = operationRuleService.getRuleValueByCode(OpRuleEnum.BOOKING_LIMIT_SAME_TIMESLOT);
        if (sameTimeSlotRule != null && sameTimeSlotRule.getEnabled() == 1 && sameTimeSlotRule.getValue() == 1) {
            MPJLambdaWrapper<Appointment> wrapper = new MPJLambdaWrapper<Appointment>()
                    .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                    .eq(Appointment::getPatientUserId, patientUserId)
                    .eq(DoctorSchedule::getScheduleDate, schedule.getScheduleDate())
                    .eq(DoctorSchedule::getSlotPeriod, schedule.getSlotPeriod())
                    .eq(Appointment::getStatus, 1);
            Long count = appointmentMapper.selectJoinCount(wrapper);
            if (count > 0) {
                return Result.error("您在该时间段已有预约，请勿重复预约");
            }
        }

        // 单日挂号次数限制
        RuleInfo ruleInfo = operationRuleService.getRuleValueByCode(OpRuleEnum.BOOKING_MAX_PER_DAY_GLOBAL);
        if (ruleInfo != null && ruleInfo.getEnabled() == 1) {
            Integer maxBookingsPerDay = ruleInfo.getValue();
            LocalDate scheduleDate = schedule.getScheduleDate();

            log.info("检查用户ID {} 在日期 {} 的挂号次数，上限为 {}", patientUserId, scheduleDate, maxBookingsPerDay);

            MPJLambdaWrapper<Appointment> dailyCountWrapper = new MPJLambdaWrapper<>();
            dailyCountWrapper
                .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                .eq(Appointment::getPatientUserId, patientUserId)
                .eq(DoctorSchedule::getScheduleDate, scheduleDate)
                .eq(Appointment::getStatus, 1);


            Long bookingCountOnDate = appointmentMapper.selectJoinCount(dailyCountWrapper);
            log.info("用户ID {} 在 {} 已挂号次数: {}", patientUserId, scheduleDate, bookingCountOnDate);

            if (bookingCountOnDate >= maxBookingsPerDay) {
                return Result.error("您在 " + scheduleDate + " 的挂号次数已达上限（" + maxBookingsPerDay + "次）");
            }
        }

        // 单日单科室挂号次数限制
        RuleInfo deptRuleInfo = operationRuleService.getRuleValueByCode(OpRuleEnum.BOOKING_MAX_PER_DAY_PER_DEPT);
        if (deptRuleInfo != null && deptRuleInfo.getEnabled() == 1) {
            Integer maxBookingsPerDept = deptRuleInfo.getValue();

            // 当前科室ID
            LambdaQueryWrapper<DoctorProfile> profileWrapper = new LambdaQueryWrapper<>();
            profileWrapper.eq(DoctorProfile::getUserId, schedule.getDoctorUserId())
                    .last("limit 1");
            DoctorProfile doctorProfile = doctorProfileMapper.selectOne(profileWrapper);

            if (doctorProfile != null && doctorProfile.getDepartmentId() != null) {
                Long departmentId = doctorProfile.getDepartmentId();
                LocalDate scheduleDate = schedule.getScheduleDate();
                log.info("检查用户ID {} 在日期 {} 对科室 {} 的挂号次数，上限为 {}",
                         patientUserId, scheduleDate, departmentId, maxBookingsPerDept);


                MPJLambdaWrapper<Appointment> deptCountWrapper = new MPJLambdaWrapper<>();
                deptCountWrapper
                    .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                    .leftJoin(DoctorProfile.class, DoctorProfile::getUserId, DoctorSchedule::getDoctorUserId)
                    .eq(Appointment::getPatientUserId, patientUserId)
                    .eq(DoctorSchedule::getScheduleDate, scheduleDate)
                    .eq(DoctorProfile::getDepartmentId, departmentId)
                    .eq(Appointment::getStatus, 1);


                Long deptBookingCountOnDate = appointmentMapper.selectJoinCount(deptCountWrapper);
                log.info("用户ID {} 在 {} 对科室 {} 已挂号次数: {}",
                         patientUserId, scheduleDate, departmentId, deptBookingCountOnDate);

                if (deptBookingCountOnDate >= maxBookingsPerDept) {
                    return Result.error("您在 " + scheduleDate + " 对该科室的挂号次数已达上限（" + maxBookingsPerDept + "次）");
                }
            }
        }

        LambdaQueryWrapper<org.hcmu.hcmupojo.entity.PatientProfile> patientProfileWrapper = new LambdaQueryWrapper<>();
        patientProfileWrapper.eq(org.hcmu.hcmupojo.entity.PatientProfile::getUserId, patientUserId)
                .eq(org.hcmu.hcmupojo.entity.PatientProfile::getIsDeleted, 0)
                .last("limit 1");
        org.hcmu.hcmupojo.entity.PatientProfile patientProfile = patientProfileMapper.selectOne(patientProfileWrapper);

        java.math.BigDecimal originalFee = schedule.getFee();
        java.math.BigDecimal actualFee = originalFee;

        if (patientProfile != null && patientProfile.getIdentityType() != null) {
            Integer identityType = patientProfile.getIdentityType();
            if (identityType == 1) {
                actualFee = originalFee.multiply(new java.math.BigDecimal("0.05"));
            } else if (identityType == 2) {
                actualFee = originalFee.multiply(new java.math.BigDecimal("0.10"));
            } else {
                return Result.error("身份类型不合法");
            }
        }

        Appointment appointment = new Appointment();
        appointment.setAppointmentNo(generateAppointmentNo(scheduleId));
        appointment.setPatientUserId(patientUserId);
        appointment.setScheduleId(scheduleId);
        appointment.setVisitNo(-1);
        appointment.setStatus(1);
        appointment.setOriginalFee(originalFee);
        appointment.setActualFee(actualFee);

        // 106规则
        RuleInfo payTimeRule = operationRuleService.getRuleValueByCode(OpRuleEnum.BOOKING_MAX_PAY_TIME);
        Integer lockMinutes = OpRuleEnum.BOOKING_MAX_PAY_TIME.getDefaultValue(); // 默认值
        if (payTimeRule != null && payTimeRule.getEnabled() == 1 && payTimeRule.getValue() != null) {
            lockMinutes = payTimeRule.getValue();
        }
        appointment.setLockExpireTime(LocalDateTime.now().plusMinutes(lockMinutes));
        log.info("创建预约，支付截止时间: {}", appointment.getLockExpireTime());

        appointmentMapper.insert(appointment);
        schedule.setAvailableSlots(Math.max(availableSlots - 1, 0));

        baseMapper.updateById(schedule);

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
                .leftJoin(Department.class, Department::getDepartmentId, DoctorProfile::getDepartmentId)
                .selectAs(Department::getName, "departmentName")
                .eq(Appointment::getAppointmentId, appointment.getAppointmentId());

        AppointmentDTO.AppointmentListDTO dto = appointmentMapper.selectJoinOne(
                AppointmentDTO.AppointmentListDTO.class,
                queryWrapper
        );

        // 发送预约成功邮件通知
        User patientUser = userMapper.selectById(patientUserId);
        if (patientUser != null && patientUser.getEmail() != null && !patientUser.getEmail().isEmpty()) {
            try {
                // 获取时段信息
                String periodDesc = "";
                PeriodEnum periodEnum = PeriodEnum.getEnumByCode(schedule.getSlotPeriod());
                if (periodEnum != null) {
                    periodDesc = periodEnum.getDesc();
                }

                // 构建邮件内容
                String subject = "预约成功通知";
                StringBuilder content = new StringBuilder();
                content.append("尊敬的 ").append(patientUser.getName()).append("，您好！\n\n");
                content.append("您的预约已成功！\n\n");
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
                content.append("挂号费：¥").append(actualFee).append("\n");
                content.append("\n请您准时付款，如有问题请及时联系医院。\n");
                content.append("\n祝您早日康复！");

                mailService.sendNotification(subject, content.toString(), patientUser.getEmail());
                log.info("预约成功邮件已发送至: {}", patientUser.getEmail());
            } catch (Exception e) {
                // 邮件发送失败不影响预约流程
                log.error("发送预约成功邮件失败: {}", e.getMessage());
            }
        }

        return Result.success("预约成功", dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<AppointmentDTO.AppointmentListDTO> appointSchedule(Long scheduleId, Long patientUserId) {
        DoctorSchedule schedule = baseMapper.selectById(scheduleId);
        if (schedule.getStatus() != null && !Integer.valueOf(1).equals(schedule.getStatus())) {
            return Result.error("当前排班暂不可预约");
        }

        // 提前天数限制
        RuleInfo futureRuleInfo = operationRuleService.getRuleValueByCode(OpRuleEnum.BOOKING_MAX_FUTURE_DAYS);
        if (futureRuleInfo != null && futureRuleInfo.getEnabled() == 1) {
            Integer maxFutureDays = futureRuleInfo.getValue();
            LocalDate maxFutureDate = LocalDate.now().plusDays(maxFutureDays);

            if (schedule.getScheduleDate().isAfter(maxFutureDate)) {
                return Result.error("该排班日期超过可预约的最大提前天数（" + maxFutureDays + "天）");
            }

            log.info("检查排班日期 {} 是否在允许范围内，最大可预约日期: {}",
                     schedule.getScheduleDate(), maxFutureDate);
        }

        // 就诊前多少小时停止预约
        RuleInfo minHoursRule = operationRuleService.getRuleValueByCode(OpRuleEnum.BOOKING_MIN_HOURS_BEFORE_BOOKING_END);
        if (minHoursRule != null && minHoursRule.getEnabled() == 1) {
            Integer minHours = minHoursRule.getValue();
            PeriodEnum periodEnum = PeriodEnum.getEnumByCode(schedule.getSlotPeriod());
            if (periodEnum != null) {
                String startTimeStr = periodEnum.getDesc().split("-")[0];
                LocalTime startTime = LocalTime.parse(startTimeStr);

                LocalDateTime scheduleDateTime = LocalDateTime.of(schedule.getScheduleDate(), startTime);
                log.info("当前时间: {}, 排班时间: {}, 预约截止时间: {}", LocalDateTime.now(), scheduleDateTime, scheduleDateTime.minusHours(minHours));
                LocalDateTime bookingEndTime = scheduleDateTime.minusHours(minHours);

                if (LocalDateTime.now().isAfter(bookingEndTime)) {
                    return Result.error("该排班已停止预约，请在就诊开始前 " + minHours + " 小时完成预约");
                }
            }
        }



        // 校验患者角色
        MPJLambdaWrapper<UserRole> roleQueryWrapper = new MPJLambdaWrapper<>();
        roleQueryWrapper.select(Role::getType)
                .leftJoin(Role.class, Role::getRoleId, UserRole::getRoleId)
                .eq(UserRole::getUserId, patientUserId);

        Role userRole = userRoleMapper.selectJoinOne(Role.class, roleQueryWrapper);
        if (userRole == null || !RoleTypeEnum.PATIENT.getCode().equals(userRole.getType())) {
            return Result.error("当前用户不是患者角色，无法预约");
        }

        // 爽约限制挂号检查
        RuleInfo noShowPunishRule = operationRuleService.getRuleValueByCode(OpRuleEnum.CANCEL_NO_SHOW_PUNISH_DAYS);
        if (noShowPunishRule != null && noShowPunishRule.getEnabled() == 1) {
            Integer punishDays = noShowPunishRule.getValue();

            // 查询用户的爽约记录（status=6）
            MPJLambdaWrapper<Appointment> noShowWrapper = new MPJLambdaWrapper<>();
            noShowWrapper
                .selectAll(Appointment.class)
                .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                .eq(Appointment::getPatientUserId, patientUserId)
                .eq(Appointment::getStatus, 6)
                .eq(Appointment::getIsDeleted, 0)
                .eq(DoctorSchedule::getIsDeleted, 0)
                .eq(DoctorSchedule::getStatus, 1)
                .orderByDesc(DoctorSchedule::getScheduleDate);

            List<Appointment> noShowAppointments = appointmentMapper.selectJoinList(Appointment.class, noShowWrapper);

            if (!noShowAppointments.isEmpty()) {
                LocalDate currentDate = LocalDate.now();

                for (Appointment noShowAppointment : noShowAppointments) {
                    // 获取爽约的排班信息
                    DoctorSchedule noShowSchedule = baseMapper.selectById(noShowAppointment.getScheduleId());
                    if (noShowSchedule != null) {
                        LocalDate noShowDate = noShowSchedule.getScheduleDate();
                        LocalDate punishmentEndDate = noShowDate.plusDays(punishDays);

                        // 当前日期在惩罚期内
                        if (currentDate.isBefore(punishmentEndDate)) {
                            log.info("用户ID {} 因爽约被限制挂号，爽约日期: {}, 惩罚结束日期: {}",
                                     patientUserId, noShowDate, punishmentEndDate);
                            return Result.error("您因爽约记录被限制挂号，限制至 " + punishmentEndDate + "，请届时再试");
                        }
                    }
                }
            }
        }

        // 防止重复预约
        LambdaQueryWrapper<Appointment> duplicateWrapper = new LambdaQueryWrapper<>();
        duplicateWrapper.eq(Appointment::getScheduleId, scheduleId)
                .eq(Appointment::getPatientUserId, patientUserId)
                .ne(Appointment::getStatus, 5)
                .ne(Appointment::getStatus, 6);
        if (appointmentMapper.selectCount(duplicateWrapper) > 0) {
            return Result.error("请勿重复预约该排班");
        }

        // 是否能同一时段多重预约限制
        RuleInfo sameTimeSlotRule = operationRuleService.getRuleValueByCode(OpRuleEnum.BOOKING_LIMIT_SAME_TIMESLOT);
        if (sameTimeSlotRule != null && sameTimeSlotRule.getEnabled() == 1 && sameTimeSlotRule.getValue() == 1) {
            MPJLambdaWrapper<Appointment> wrapper = new MPJLambdaWrapper<Appointment>()
                    .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                    .eq(Appointment::getPatientUserId, patientUserId)
                    .eq(DoctorSchedule::getScheduleDate, schedule.getScheduleDate())
                    .eq(DoctorSchedule::getSlotPeriod, schedule.getSlotPeriod())
                    .eq(Appointment::getStatus, 1);
            Long count = appointmentMapper.selectJoinCount(wrapper);
            if (count > 0) {
                return Result.error("您在该时间段已有预约，请勿重复预约");
            }
        }

        // 单日挂号次数限制
        RuleInfo ruleInfo = operationRuleService.getRuleValueByCode(OpRuleEnum.BOOKING_MAX_PER_DAY_GLOBAL);
        if (ruleInfo != null && ruleInfo.getEnabled() == 1) {
            Integer maxBookingsPerDay = ruleInfo.getValue();
            LocalDate scheduleDate = schedule.getScheduleDate();

            log.info("检查用户ID {} 在日期 {} 的挂号次数，上限为 {}", patientUserId, scheduleDate, maxBookingsPerDay);

            MPJLambdaWrapper<Appointment> dailyCountWrapper = new MPJLambdaWrapper<>();
            dailyCountWrapper
                .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                .eq(Appointment::getPatientUserId, patientUserId)
                .eq(DoctorSchedule::getScheduleDate, scheduleDate)
                .eq(Appointment::getStatus, 1);


            Long bookingCountOnDate = appointmentMapper.selectJoinCount(dailyCountWrapper);
            log.info("用户ID {} 在 {} 已挂号次数: {}", patientUserId, scheduleDate, bookingCountOnDate);

            if (bookingCountOnDate >= maxBookingsPerDay) {
                return Result.error("您在 " + scheduleDate + " 的挂号次数已达上限（" + maxBookingsPerDay + "次）");
            }
        }

        // 单日单科室挂号次数限制
        RuleInfo deptRuleInfo = operationRuleService.getRuleValueByCode(OpRuleEnum.BOOKING_MAX_PER_DAY_PER_DEPT);
        if (deptRuleInfo != null && deptRuleInfo.getEnabled() == 1) {
            Integer maxBookingsPerDept = deptRuleInfo.getValue();

            // 当前科室ID
            LambdaQueryWrapper<DoctorProfile> profileWrapper = new LambdaQueryWrapper<>();
            profileWrapper.eq(DoctorProfile::getUserId, schedule.getDoctorUserId())
                    .last("limit 1");
            DoctorProfile doctorProfile = doctorProfileMapper.selectOne(profileWrapper);

            if (doctorProfile != null && doctorProfile.getDepartmentId() != null) {
                Long departmentId = doctorProfile.getDepartmentId();
                LocalDate scheduleDate = schedule.getScheduleDate();
                log.info("检查用户ID {} 在日期 {} 对科室 {} 的挂号次数，上限为 {}",
                         patientUserId, scheduleDate, departmentId, maxBookingsPerDept);


                MPJLambdaWrapper<Appointment> deptCountWrapper = new MPJLambdaWrapper<>();
                deptCountWrapper
                    .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                    .leftJoin(DoctorProfile.class, DoctorProfile::getUserId, DoctorSchedule::getDoctorUserId)
                    .eq(Appointment::getPatientUserId, patientUserId)
                    .eq(DoctorSchedule::getScheduleDate, scheduleDate)
                    .eq(DoctorProfile::getDepartmentId, departmentId)
                    .eq(Appointment::getStatus, 1);


                Long deptBookingCountOnDate = appointmentMapper.selectJoinCount(deptCountWrapper);
                log.info("用户ID {} 在 {} 对科室 {} 已挂号次数: {}",
                         patientUserId, scheduleDate, departmentId, deptBookingCountOnDate);

                if (deptBookingCountOnDate >= maxBookingsPerDept) {
                    return Result.error("您在 " + scheduleDate + " 对该科室的挂号次数已达上限（" + maxBookingsPerDept + "次）");
                }
            }
        }

        LambdaQueryWrapper<org.hcmu.hcmupojo.entity.PatientProfile> patientProfileWrapper = new LambdaQueryWrapper<>();
        patientProfileWrapper.eq(org.hcmu.hcmupojo.entity.PatientProfile::getUserId, patientUserId)
                .eq(org.hcmu.hcmupojo.entity.PatientProfile::getIsDeleted, 0)
                .last("limit 1");
        org.hcmu.hcmupojo.entity.PatientProfile patientProfile = patientProfileMapper.selectOne(patientProfileWrapper);

        java.math.BigDecimal originalFee = schedule.getFee();
        java.math.BigDecimal actualFee = originalFee;

        if (patientProfile != null && patientProfile.getIdentityType() != null) {
            Integer identityType = patientProfile.getIdentityType();
            if (identityType == 1) {
                actualFee = originalFee.multiply(new java.math.BigDecimal("0.05"));
            } else if (identityType == 2) {
                actualFee = originalFee.multiply(new java.math.BigDecimal("0.10"));
            } else {
                return Result.error("身份类型不合法");
            }
        }

        Appointment appointment = new Appointment();
        appointment.setAppointmentNo(generateAppointmentNo(scheduleId));
        appointment.setPatientUserId(patientUserId);
        appointment.setScheduleId(scheduleId);
        appointment.setVisitNo(-1);
        appointment.setStatus(1);
        appointment.setOriginalFee(originalFee);
        appointment.setActualFee(actualFee);

        // 106规则
        RuleInfo payTimeRule = operationRuleService.getRuleValueByCode(OpRuleEnum.BOOKING_MAX_PAY_TIME);
        Integer lockMinutes = OpRuleEnum.BOOKING_MAX_PAY_TIME.getDefaultValue(); // 默认值
        if (payTimeRule != null && payTimeRule.getEnabled() == 1 && payTimeRule.getValue() != null) {
            lockMinutes = payTimeRule.getValue();
        }
        appointment.setLockExpireTime(LocalDateTime.now().plusMinutes(lockMinutes));
        log.info("创建预约，支付截止时间: {}", appointment.getLockExpireTime());

        appointmentMapper.insert(appointment);
        

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
                .leftJoin(Department.class, Department::getDepartmentId, DoctorProfile::getDepartmentId)
                .selectAs(Department::getName, "departmentName")
                .eq(Appointment::getAppointmentId, appointment.getAppointmentId());

        AppointmentDTO.AppointmentListDTO dto = appointmentMapper.selectJoinOne(
                AppointmentDTO.AppointmentListDTO.class,
                queryWrapper
        );

        // 发送预约成功邮件通知
        User patientUser = userMapper.selectById(patientUserId);
        if (patientUser != null && patientUser.getEmail() != null && !patientUser.getEmail().isEmpty()) {
            try {
                // 获取时段信息
                String periodDesc = "";
                PeriodEnum periodEnum = PeriodEnum.getEnumByCode(schedule.getSlotPeriod());
                if (periodEnum != null) {
                    periodDesc = periodEnum.getDesc();
                }

                // 构建邮件内容
                String subject = "预约成功通知";
                StringBuilder content = new StringBuilder();
                content.append("尊敬的 ").append(patientUser.getName()).append("，您好！\n\n");
                content.append("您的预约已成功！\n\n");
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
                content.append("挂号费：¥").append(actualFee).append("\n");
                content.append("\n请您准时付款，如有问题请及时联系医院。\n");
                content.append("\n祝您早日康复！");

                mailService.sendNotification(subject, content.toString(), patientUser.getEmail());
                log.info("预约成功邮件已发送至: {}", patientUser.getEmail());
            } catch (Exception e) {
                // 邮件发送失败不影响预约流程
                log.error("发送预约成功邮件失败: {}", e.getMessage());
            }
        }

        return Result.success("预约成功", dto);
    }

    private String generateAppointmentNo(Long scheduleId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return "AP" + scheduleId + timestamp + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    @Override
    public Result<List<ScheduleDTO.ScheduleListDTO>> getDoctorSchedules(Long doctorUserId) {
        // 校验医生是否存在
        User user = userMapper.selectById(doctorUserId);
        if (user == null || user.getIsDeleted() == 1) {
            return Result.error("医生用户不存在");
        }

        // 校验是否为医生角色
        MPJLambdaWrapper<UserRole> roleQueryWrapper = new MPJLambdaWrapper<>();
        roleQueryWrapper.select(Role::getType)
                .leftJoin(Role.class, Role::getRoleId, UserRole::getRoleId)
                .eq(UserRole::getUserId, doctorUserId)
                .eq(UserRole::getIsDeleted, 0)
                .eq(Role::getIsDeleted, 0);

        Role userRole = userRoleMapper.selectJoinOne(Role.class, roleQueryWrapper);
        if (userRole == null) {
            return Result.error("用户未分配角色");
        }

        if (!RoleTypeEnum.DOCTOR.getCode().equals(userRole.getType())) {
            return Result.error("用户不是医生角色");
        }

        MPJLambdaWrapper<DoctorSchedule> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(DoctorSchedule::getScheduleId, DoctorSchedule::getDoctorUserId,
                        DoctorSchedule::getScheduleDate, DoctorSchedule::getSlotType, DoctorSchedule::getSlotPeriod,
                        DoctorSchedule::getTotalSlots, DoctorSchedule::getAvailableSlots, DoctorSchedule::getFee,
                        DoctorSchedule::getStatus, DoctorSchedule::getCreateTime)
                .eq(DoctorSchedule::getDoctorUserId, doctorUserId)
                .eq(DoctorSchedule::getIsDeleted, 0)
                .eq(DoctorSchedule::getStatus, 1)
                .orderByDesc(DoctorSchedule::getCreateTime);

        List<ScheduleDTO.ScheduleListDTO> scheduleList = baseMapper.selectJoinList(
                ScheduleDTO.ScheduleListDTO.class,
                queryWrapper);

        return Result.success(scheduleList);
    }

    @Override
    public Result<List<ScheduleDTO.SchedulePatientDTO>> getSchedulePatients(Long doctorUserId, Long scheduleId) {

        User doctor = userMapper.selectById(doctorUserId);
        if (doctor == null || doctor.getIsDeleted() == 1) {
            return Result.error("医生用户不存在");
        }

        MPJLambdaWrapper<UserRole> roleQueryWrapper = new MPJLambdaWrapper<>();
        roleQueryWrapper.select(Role::getType)
                .leftJoin(Role.class, Role::getRoleId, UserRole::getRoleId)
                .eq(UserRole::getUserId, doctorUserId)
                .eq(UserRole::getIsDeleted, 0)
                .eq(Role::getIsDeleted, 0);

        Role userRole = userRoleMapper.selectJoinOne(Role.class, roleQueryWrapper);
        if (userRole == null) {
            return Result.error("用户未分配角色");
        }

        if (!RoleTypeEnum.DOCTOR.getCode().equals(userRole.getType())) {
            return Result.error("用户不是医生角色");
        }

        DoctorSchedule schedule = baseMapper.selectById(scheduleId);
        if (schedule == null || schedule.getIsDeleted() == 1) {
            return Result.error("排班不存在");
        }

        if (!schedule.getDoctorUserId().equals(doctorUserId)) {
            return Result.error("该排班不属于该医生");
        }

        MPJLambdaWrapper<Appointment> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.selectAs(User::getUserId, ScheduleDTO.SchedulePatientDTO::getUserId)
                .selectAs(User::getUserName, ScheduleDTO.SchedulePatientDTO::getUserName)
                .selectAs(User::getName, ScheduleDTO.SchedulePatientDTO::getName)
                .selectAs(User::getSex, ScheduleDTO.SchedulePatientDTO::getSex)
                .selectAs(User::getEmail, ScheduleDTO.SchedulePatientDTO::getEmail)
                .selectAs(User::getPhone, ScheduleDTO.SchedulePatientDTO::getPhone)
                .leftJoin(User.class, User::getUserId, Appointment::getPatientUserId)
                .eq(Appointment::getScheduleId, scheduleId)
                .ne(Appointment::getStatus, 0);


        List<ScheduleDTO.SchedulePatientDTO> patientList = appointmentMapper.selectJoinList(
                ScheduleDTO.SchedulePatientDTO.class,
                queryWrapper);

        return Result.success(patientList);
    }
}
