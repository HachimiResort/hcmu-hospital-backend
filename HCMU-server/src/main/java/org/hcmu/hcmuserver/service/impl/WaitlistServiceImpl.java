package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.OpRuleEnum;
import org.hcmu.hcmucommon.enumeration.PeriodEnum;
import org.hcmu.hcmucommon.enumeration.RoleTypeEnum;
import org.hcmu.hcmucommon.enumeration.WaitListEnum;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.AppointmentDTO;
import org.hcmu.hcmupojo.dto.OperationRuleDTO.RuleInfo;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.WaitlistDTO;
import org.hcmu.hcmupojo.entity.Department;
import org.hcmu.hcmupojo.entity.DoctorProfile;
import org.hcmu.hcmupojo.entity.DoctorSchedule;
import org.hcmu.hcmupojo.entity.PatientProfile;
import org.hcmu.hcmupojo.entity.Role;
import org.hcmu.hcmupojo.entity.User;
import org.hcmu.hcmupojo.entity.Waitlist;
import org.hcmu.hcmupojo.entity.relation.UserRole;
import org.hcmu.hcmuserver.mapper.Waitlist.WaitlistMapper;
import org.hcmu.hcmuserver.mapper.appointment.AppointmentMapper;
import org.hcmu.hcmuserver.mapper.department.DepartmentMapper;
import org.hcmu.hcmuserver.mapper.doctorprofile.DoctorProfileMapper;
import org.hcmu.hcmuserver.mapper.patientprofile.PatientProfileMapper;
import org.hcmu.hcmuserver.mapper.role.RoleMapper;
import org.hcmu.hcmuserver.mapper.schedule.ScheduleMapper;
import org.hcmu.hcmuserver.mapper.user.UserMapper;
import org.hcmu.hcmuserver.mapper.user.UserRoleMapper;
import org.hcmu.hcmuserver.service.MailService;
import org.hcmu.hcmuserver.service.OperationRuleService;
import org.hcmu.hcmuserver.service.ScheduleService;
import org.hcmu.hcmuserver.service.WaitlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
public class WaitlistServiceImpl extends MPJBaseServiceImpl<WaitlistMapper, Waitlist> implements WaitlistService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private MailService mailService;

    @Autowired
    private DoctorProfileMapper doctorProfileMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private PatientProfileMapper patientProfileMapper;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private OperationRuleService operationRuleService;

    @Autowired
    private AppointmentMapper appointmentMapper;

    private int getLockExpireMinutes() {
        RuleInfo ruleInfo = operationRuleService.getRuleValueByCode(OpRuleEnum.BOOKING_MAX_PAY_TIME);
        if (ruleInfo != null && ruleInfo.getEnabled() == 1 && ruleInfo.getValue() != null) {
            return ruleInfo.getValue();
        }
        return OpRuleEnum.BOOKING_MAX_PAY_TIME.getDefaultValue();
    }

    @Override
    public Result<WaitlistDTO.WaitlistFullDTO> createWaitlist(WaitlistDTO.WaitlistCreateDTO createDTO) {

        User user = userMapper.selectById(createDTO.getPatientUserId());
        if (user == null) {
            return Result.error("患者不存在");
        }

        LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(UserRole::getUserId, createDTO.getPatientUserId());
        UserRole userRole = userRoleMapper.selectOne(userRoleWrapper);

        if (userRole == null) {
            return Result.error("该用户未分配角色");
        }

        Role role = roleMapper.selectById(userRole.getRoleId());
        if (role == null || !Objects.equals(role.getType(), RoleTypeEnum.PATIENT.getCode())) {
            return Result.error("该用户不是患者");
        }

        DoctorSchedule schedule = scheduleMapper.selectById(createDTO.getScheduleId());
        if (schedule == null) {
            return Result.error("排班不存在");
        }

        LambdaQueryWrapper<Waitlist> duplicateWrapper = new LambdaQueryWrapper<>();
        duplicateWrapper.eq(Waitlist::getPatientUserId, createDTO.getPatientUserId())
                .eq(Waitlist::getScheduleId, createDTO.getScheduleId())
                .ne(Waitlist::getStatus, WaitListEnum.EXPIRED.getCode())
                .ne(Waitlist::getStatus, WaitListEnum.CANCELLED.getCode())
                .ne(Waitlist::getStatus, WaitListEnum.BOOKED.getCode());

        if (baseMapper.selectCount(duplicateWrapper) > 0) {
            return Result.error("该患者已在该排班的等待队列中");
        }

        Waitlist waitlist = Waitlist.builder()
                .patientUserId(createDTO.getPatientUserId())
                .scheduleId(createDTO.getScheduleId())
                .status(createDTO.getStatus())
                .build();

        baseMapper.insert(waitlist);

        return getWaitlistById(waitlist.getWaitlistId());
    }


    @Override
    public Result<PageDTO<WaitlistDTO.WaitlistListDTO>> getWaitlists(WaitlistDTO.WaitlistGetRequestDTO requestDTO) {
        MPJLambdaWrapper<Waitlist> queryWrapper = new MPJLambdaWrapper<>();

        // 选择 Waitlist 的基本字段
        queryWrapper.select(Waitlist::getWaitlistId,
                        Waitlist::getPatientUserId,
                        Waitlist::getScheduleId,
                        Waitlist::getStatus,
                        Waitlist::getCreateTime)
                // 关联用户表获取患者信息
                .leftJoin(User.class, User::getUserId, Waitlist::getPatientUserId)
                .selectAs(User::getUserName, "patientUserName")
                .selectAs(User::getPhone, "patientPhone")
                .eq(ObjectUtils.isNotEmpty(requestDTO.getPatientUserId()),
                        Waitlist::getPatientUserId, requestDTO.getPatientUserId())
                .eq(ObjectUtils.isNotEmpty(requestDTO.getScheduleId()),
                        Waitlist::getScheduleId, requestDTO.getScheduleId())
                .eq(ObjectUtils.isNotEmpty(requestDTO.getStatus()),
                        Waitlist::getStatus, requestDTO.getStatus())

                .orderByDesc(Waitlist::getCreateTime);

        // 执行分页查询
        IPage<WaitlistDTO.WaitlistListDTO> page = baseMapper.selectJoinPage(
                new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize()),
                WaitlistDTO.WaitlistListDTO.class,
                queryWrapper
        );

        return Result.success(new PageDTO<>(page));
    }

    @Override
    public Result<WaitlistDTO.WaitlistFullDTO> getWaitlistById(Long waitlistId) {
        // 先验证 waitlistId 是否存在
        Waitlist waitlist = baseMapper.selectById(waitlistId);
        if (waitlist == null) {
            return Result.error("候补记录不存在或已被删除");
        }



        MPJLambdaWrapper<Waitlist> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper
                .select(Waitlist::getWaitlistId,
                        Waitlist::getPatientUserId,
                        Waitlist::getScheduleId,
                        Waitlist::getStatus,
                        Waitlist::getNotifiedTime,
                        Waitlist::getLockExpireTime,
                        Waitlist::getCreateTime)

                .leftJoin(User.class, User::getUserId, Waitlist::getPatientUserId)
                .selectAs(User::getUserName, "patientUserName")
                .selectAs(User::getPhone, "patientPhone")
                .selectAs(User::getName, "patientName")

                .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Waitlist::getScheduleId)
                .selectAs(DoctorSchedule::getScheduleDate, "scheduleDate")
                .selectAs(DoctorSchedule::getSlotType, "slotType")
                .selectAs(DoctorSchedule::getSlotPeriod, "slotPeriod")
                .select("t2.doctor_user_id AS doctorUserId")

                .leftJoin("user AS doctor_user ON doctor_user.user_id = t2.doctor_user_id")
                .select("doctor_user.name AS doctorName")

                .leftJoin("doctor_profile ON doctor_profile.user_id = t2.doctor_user_id")
                .select("doctor_profile.title AS doctorTitle")

                .leftJoin("department ON department.department_id = doctor_profile.department_id")
                .select("department.name AS departmentName")
                .eq(Waitlist::getWaitlistId, waitlistId);

        WaitlistDTO.WaitlistFullDTO fullDTO = baseMapper.selectJoinOne(
                WaitlistDTO.WaitlistFullDTO.class, queryWrapper);

        if (fullDTO == null) {
            return Result.error("候补记录不存在或已被删除");
        }

        // 计算实际费用
        DoctorSchedule schedule = scheduleMapper.selectById(fullDTO.getScheduleId());
        if (schedule != null && schedule.getFee() != null) {
            java.math.BigDecimal originalFee = schedule.getFee();
            java.math.BigDecimal actualFee = originalFee;

            log.info("候补ID {} - 原始费用: {}", waitlistId, originalFee);

            LambdaQueryWrapper<PatientProfile> patientProfileWrapper = new LambdaQueryWrapper<>();
            patientProfileWrapper.eq(PatientProfile::getUserId, fullDTO.getPatientUserId())
                    .last("LIMIT 1");
            PatientProfile patientProfile = patientProfileMapper.selectOne(patientProfileWrapper);

            if (patientProfile != null) {
                log.info("候补ID {} - 找到患者档案，身份类型: {}", waitlistId, patientProfile.getIdentityType());

                if (patientProfile.getIdentityType() != null) {
                    Integer identityType = patientProfile.getIdentityType();
                    if (identityType == 1) {
                        // 学生：10% 费用
                        actualFee = originalFee.multiply(new java.math.BigDecimal("0.10"));
                        log.info("候补ID {} - 应用学生折扣(10%)，实际费用: {}", waitlistId, actualFee);
                    } else if (identityType == 2) {
                        // 老师：20% 费用
                        actualFee = originalFee.multiply(new java.math.BigDecimal("0.20"));
                        log.info("候补ID {} - 应用教师折扣(20%)，实际费用: {}", waitlistId, actualFee);
                    } else {
                        log.info("候补ID {} - 身份类型为 {}，不应用折扣", waitlistId, identityType);
                    }
                } else {
                    log.warn("候补ID {} - 患者档案存在但身份类型为空", waitlistId);
                }
            } else {
                log.warn("候补ID {} - 未找到患者档案，患者ID: {}", waitlistId, fullDTO.getPatientUserId());
            }

            fullDTO.setActualFee(actualFee);
            log.info("候补ID {} - 最终返回费用: {}", waitlistId, actualFee);
        } else {
            log.warn("候补ID {} - 排班或费用信息不存在", waitlistId);
        }

        return Result.success(fullDTO);
    }

    @Override
    public Result<String> updateWaitlistById(Long waitlistId, WaitlistDTO.WaitlistUpdateDTO updateDTO) {
        Waitlist waitlist = baseMapper.selectById(waitlistId);
        if (waitlist == null) {
            return Result.error("候诊记录不存在或已被删除");
        }

        updateDTO.updateWaitlist(waitlist);
        baseMapper.updateById(waitlist);
        return Result.success("更新成功");
    }

    @Override
    public Result<String> deleteWaitlistById(Long waitlistId) {
        Waitlist waitlist = baseMapper.selectById(waitlistId);
        if (waitlist == null) {
            return Result.error("候诊记录不存在或已被删除");
        }

        baseMapper.deleteById(waitlistId);
        return Result.success("删除成功");
    }

    /**
     * 用户申请加入候补队列
     * @param joinDTO
     * @return
     */
    @Override
    public Result<WaitlistDTO.WaitlistFullDTO> patientJoinWaitlist(WaitlistDTO.PatientJoinDTO joinDTO) {

        User user = userMapper.selectById(joinDTO.getUserId());
        if (user == null) {
            return Result.error("用户不存在");
        }

        LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(UserRole::getUserId, joinDTO.getUserId());
        UserRole userRole = userRoleMapper.selectOne(userRoleWrapper);

        if (userRole == null) {
            return Result.error("该用户未分配角色");
        }

        Role role = roleMapper.selectById(userRole.getRoleId());
        if (role == null || !Objects.equals(role.getType(), RoleTypeEnum.PATIENT.getCode())) {
            return Result.error("该用户不是患者");
        }

        LambdaQueryWrapper<DoctorSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(DoctorSchedule::getScheduleId, joinDTO.getScheduleId())
                .ne(DoctorSchedule::getStatus, 0);
        DoctorSchedule schedule = scheduleMapper.selectOne(scheduleWrapper);

        if (schedule == null) {
            return Result.error("排班不存在或已关闭");
        }

        if (schedule.getAvailableSlots() != null && schedule.getAvailableSlots() > 0) {
            return Result.error("当前排班还有可用号源，无需候补");
        }


        LambdaQueryWrapper<org.hcmu.hcmupojo.entity.Appointment> appointmentWrapper = new LambdaQueryWrapper<>();
        appointmentWrapper.eq(org.hcmu.hcmupojo.entity.Appointment::getPatientUserId, joinDTO.getUserId())
                .eq(org.hcmu.hcmupojo.entity.Appointment::getScheduleId, joinDTO.getScheduleId())
                .eq(org.hcmu.hcmupojo.entity.Appointment::getIsDeleted, 0)
                .in(org.hcmu.hcmupojo.entity.Appointment::getStatus, 1, 2, 3, 4);
        if (appointmentMapper.selectCount(appointmentWrapper) > 0) {
            return Result.error("您已预约该排班，无需候补");
        }

        // 检查是否已在候补队列中（排除已取消和已过期）
        LambdaQueryWrapper<Waitlist> duplicateWrapper = new LambdaQueryWrapper<>();
        duplicateWrapper.eq(Waitlist::getPatientUserId, joinDTO.getUserId())
                .eq(Waitlist::getScheduleId, joinDTO.getScheduleId())
                .in(Waitlist::getStatus, WaitListEnum.WAITING.getCode(),
                                        WaitListEnum.NOTIFIED.getCode());
        if (baseMapper.selectCount(duplicateWrapper) > 0) {
            return Result.error("您已在该排班的候补队列中");
        }

        Waitlist waitlist = Waitlist.builder()
                .patientUserId(joinDTO.getUserId())
                .scheduleId(joinDTO.getScheduleId())
                .status(WaitListEnum.WAITING.getCode())
                .build();

        baseMapper.insert(waitlist);

        log.info("用户ID {} 成功加入排班ID {} 的候补队列,候补ID: {}",
            joinDTO.getUserId(), joinDTO.getScheduleId(), waitlist.getWaitlistId());

        return getWaitlistById(waitlist.getWaitlistId());
    }

    /**
     * 通知候补队列中的下一位患者
     * @param scheduleId 排班ID
     * @return
     */
    @Override
    public boolean notifyNextWaitlist(Long scheduleId) {
        LambdaQueryWrapper<Waitlist> waitlistWrapper = new LambdaQueryWrapper<>();
        waitlistWrapper.eq(Waitlist::getScheduleId, scheduleId)
                .eq(Waitlist::getStatus, WaitListEnum.WAITING.getCode())
                .orderByAsc(Waitlist::getCreateTime)
                .last("LIMIT 1");

        Waitlist nextWaitlist = baseMapper.selectOne(waitlistWrapper);

        if (nextWaitlist == null) {
            log.info("排班ID {} 没有候补患者", scheduleId);
            return false;
        }

        // 更新候补状态
        LocalDateTime now = LocalDateTime.now();



        nextWaitlist.setStatus(WaitListEnum.NOTIFIED.getCode());
        nextWaitlist.setNotifiedTime(now);
        nextWaitlist.setLockExpireTime(now.plusMinutes(getLockExpireMinutes()));
        nextWaitlist.setUpdateTime(now);
        baseMapper.updateById(nextWaitlist);

        log.info("候补ID {} 已通知 - 当前时间: {}, 通知时间: {}, 支付截止时间: {}",
            nextWaitlist.getWaitlistId(), now, nextWaitlist.getNotifiedTime(), nextWaitlist.getLockExpireTime());


        try {
            User patient = userMapper.selectById(nextWaitlist.getPatientUserId());
            if (patient == null || patient.getEmail() == null || patient.getEmail().isEmpty()) {
                log.warn("候补患者ID {} 没有邮箱，无法发送通知", nextWaitlist.getPatientUserId());
                return true;
            }

            DoctorSchedule schedule = scheduleMapper.selectById(scheduleId);
            if (schedule == null) {
                log.warn("排班ID {} 不存在，无法发送通知", scheduleId);
                return true;
            }

            User doctor = userMapper.selectById(schedule.getDoctorUserId());
            String doctorName = doctor != null ? doctor.getName() : "🦆🦆";

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

            String subject = "候补成功通知";
            StringBuilder content = new StringBuilder();
            content.append("尊敬的 ").append(patient.getName()).append("，您好！\n\n");
            content.append("恭喜您！您候补的号源已经释放，请及时完成支付。\n\n");
            content.append("预约信息如下：\n");
            content.append("就诊日期：").append(schedule.getScheduleDate()).append("\n");
            content.append("就诊时段：").append(periodDesc).append("\n");
            if (!departmentName.isEmpty()) {
                content.append("科室：").append(departmentName).append("\n");
            }
            content.append("医生：").append(doctorName).append("\n");
            content.append("挂号费：¥").append(schedule.getFee()).append("\n");
            content.append("\n【重要提醒】\n");
            content.append("请在 ").append(nextWaitlist.getLockExpireTime()).append(" 前完成支付，");
            content.append("超时将自动转给下一位候补患者。\n");
            content.append("\n祝您早日康复！");

            mailService.sendNotification(subject, content.toString(), patient.getEmail());
            log.info("候补成功通知邮件已发送至: {}", patient.getEmail());
        } catch (Exception e) {
            log.error("发送候补通知邮件失败: {}", e.getMessage());
        }

        return true;
    }

    /**
     * 候补成功后支付并正式加入预约
     * @param waitlistId 候补ID
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<WaitlistDTO.WaitlistFullDTO> payWaitlist(Long waitlistId) {

        Waitlist waitlist = baseMapper.selectById(waitlistId);
        if (waitlist == null) {
            return Result.error("候补记录不存在");
        }

        // 进入当前方法时，状态必须是已通知
        if (!WaitListEnum.NOTIFIED.getCode().equals(waitlist.getStatus())) {
            return Result.error("该候补尚未通知或已失效，当前状态: " + waitlist.getStatus());
        }

        // 检查是否超时
        if (waitlist.getLockExpireTime() == null || LocalDateTime.now().isAfter(waitlist.getLockExpireTime())) {
            return Result.error("支付超时，请重新候补或预约其他时段");
        }

        DoctorSchedule schedule = scheduleMapper.selectById(waitlist.getScheduleId());
        if (schedule == null) {
            return Result.error("排班不存在");
        }

        if (schedule.getStatus() == null || schedule.getStatus() != 1) {
            return Result.error("该排班已关闭，无法预约");
        }

        // 预约（号源已在取消预约时恢复，直接调用正常预约流程即可）
        Result<AppointmentDTO.AppointmentListDTO> appointResult =
                scheduleService.appointSchedule(waitlist.getScheduleId(), waitlist.getPatientUserId());

        if (appointResult.getCode() != 200) {
            return Result.error(appointResult.getMsg());
        }

        // 更新为"已支付"
        AppointmentDTO.AppointmentListDTO appointmentDTO = appointResult.getData();
        if (appointmentDTO != null && appointmentDTO.getAppointmentId() != null) {
            org.hcmu.hcmupojo.entity.Appointment appointment =
                appointmentMapper.selectById(appointmentDTO.getAppointmentId());
            if (appointment != null) {
                appointment.setStatus(2);
                appointment.setPaymentTime(LocalDateTime.now());
                appointmentMapper.updateById(appointment);
                log.info("候补ID {} 创建的预约ID {} 已更新为已支付状态", waitlistId, appointment.getAppointmentId());
            }
        }

        waitlist.setStatus(WaitListEnum.BOOKED.getCode());
        waitlist.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(waitlist);

        log.info("候补ID {} 支付成功，已转为预约", waitlistId);

        // 返回完整的候补信息
        return getWaitlistById(waitlistId);
    }

    /**
     * 患者取消候补
     * @param waitlistId 候补ID
     * @return 操作结果
     */
    @Override
    public Result<WaitlistDTO.WaitlistFullDTO> cancelWaitlist(Long waitlistId) {

        Waitlist waitlist = baseMapper.selectById(waitlistId);
        if (waitlist == null) {
            return Result.error("候补记录不存在");
        }


        if (!WaitListEnum.WAITING.getCode().equals(waitlist.getStatus())) {
            return Result.error("只有候补中状态才能取消候补");
        }

        waitlist.setStatus(WaitListEnum.CANCELLED.getCode());
        waitlist.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(waitlist);

        log.info("候补ID {} 已被患者取消", waitlistId);

        // 返回完整的候补信息
        return getWaitlistById(waitlistId);
    }

    @Override
    public Result<java.util.List<WaitlistDTO.WaitlistFullDTO>> getWaitlistsByUserId(Long userId) {
        log.info("查询用户ID {} 的候补列表", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        LambdaQueryWrapper<Waitlist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Waitlist::getPatientUserId, userId)
                .orderByDesc(Waitlist::getCreateTime);

        java.util.List<Waitlist> waitlistList = this.list(queryWrapper);

        java.util.List<WaitlistDTO.WaitlistFullDTO> fullDTOList = new java.util.ArrayList<>();
        for (Waitlist waitlist : waitlistList) {
            Result<WaitlistDTO.WaitlistFullDTO> result = getWaitlistById(waitlist.getWaitlistId());
            if (result.getCode() == 200 && result.getData() != null) {
                fullDTOList.add(result.getData());
            }
        }

        log.info("用户ID {} 的候补列表查询成功，共 {} 条记录", userId, fullDTOList.size());

        return Result.success(fullDTOList);
    }

}
