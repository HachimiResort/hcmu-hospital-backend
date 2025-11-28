package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.RoleTypeEnum;
import org.hcmu.hcmucommon.enumeration.ScheduleRequestStatusEnum;
import org.hcmu.hcmucommon.enumeration.ScheduleRequestTypeEnum;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.LoginUser;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.ScheduleRequestDTO;
import org.hcmu.hcmupojo.entity.Appointment;
import org.hcmu.hcmupojo.entity.DoctorSchedule;
import org.hcmu.hcmupojo.entity.Role;
import org.hcmu.hcmupojo.entity.ScheduleRequest;
import org.hcmu.hcmupojo.entity.User;
import org.hcmu.hcmupojo.entity.relation.UserRole;
import org.hcmu.hcmuserver.mapper.appointment.AppointmentMapper;
import org.hcmu.hcmuserver.mapper.schedulerequest.ScheduleRequestMapper;
import org.hcmu.hcmuserver.mapper.schedule.ScheduleMapper;
import org.hcmu.hcmuserver.mapper.user.UserMapper;
import org.hcmu.hcmuserver.mapper.user.UserRoleMapper;
import org.hcmu.hcmuserver.service.ScheduleRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Slf4j
public class ScheduleRequestServiceImpl extends MPJBaseServiceImpl<ScheduleRequestMapper, ScheduleRequest> implements ScheduleRequestService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public Result<ScheduleRequestDTO.ScheduleRequestDetailDTO> createScheduleRequest(ScheduleRequestDTO.ScheduleRequestCreateDTO createDTO) {

        User doctor = userMapper.selectById(createDTO.getDoctorUserId());
        if (doctor == null) {
            return Result.error("医生用户不存在");
        }

        if (!isDoctorUser(createDTO.getDoctorUserId())) {
            return Result.error("该用户不是医生角色");
        }

        DoctorSchedule schedule = scheduleMapper.selectById(createDTO.getScheduleId());
        if (schedule == null ) {
            return Result.error("排班不存在");
        }

        if (!schedule.getDoctorUserId().equals(createDTO.getDoctorUserId())) {
            return Result.error("该排班不属于该医生");
        }

        Result<String> validate = validateTypeParams(createDTO.getRequestType(),
                createDTO.getTargetDate(),
                createDTO.getTargetSlotPeriod(),
                createDTO.getExtraSlots());
        if (validate.getCode() != 200) {
            return Result.error(validate.getMsg());
        }

        ScheduleRequest request = ScheduleRequest.builder()
                .doctorUserId(createDTO.getDoctorUserId())
                .scheduleId(createDTO.getScheduleId())
                .requestType(createDTO.getRequestType())
                .targetDate(createDTO.getTargetDate())
                .targetSlotPeriod(createDTO.getTargetSlotPeriod())
                .targetSlotType(createDTO.getTargetSlotType())
                .extraSlots(createDTO.getExtraSlots())
                .reason(createDTO.getReason())
                .status(ScheduleRequestStatusEnum.PENDING.getCode())
                .build();

        baseMapper.insert(request);
        return getScheduleRequestById(request.getRequestId());
    }

    @Override
    public Result<PageDTO<ScheduleRequestDTO.ScheduleRequestListDTO>> getScheduleRequests(ScheduleRequestDTO.ScheduleRequestGetRequestDTO requestDTO) {
        MPJLambdaWrapper<ScheduleRequest> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper
                .select(ScheduleRequest::getRequestId,
                        ScheduleRequest::getDoctorUserId,
                        ScheduleRequest::getScheduleId,
                        ScheduleRequest::getRequestType,
                        ScheduleRequest::getStatus,
                        ScheduleRequest::getTargetDate,
                        ScheduleRequest::getTargetSlotPeriod,
                        ScheduleRequest::getTargetSlotType,
                        ScheduleRequest::getExtraSlots,
                        ScheduleRequest::getReason,
                        ScheduleRequest::getApproverUserId,
                        ScheduleRequest::getApproveRemark,
                        ScheduleRequest::getApproveTime,
                        ScheduleRequest::getCreateTime)
                .leftJoin(User.class, User::getUserId, ScheduleRequest::getDoctorUserId)
                .selectAs(User::getName, "doctorName")
                .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, ScheduleRequest::getScheduleId)
                .selectAs(DoctorSchedule::getScheduleDate, "scheduleDate")
                .selectAs(DoctorSchedule::getSlotPeriod, "slotPeriod")
                .selectAs(DoctorSchedule::getSlotType, "slotType")
                .eq(requestDTO.getDoctorUserId() != null, ScheduleRequest::getDoctorUserId, requestDTO.getDoctorUserId())
                .eq(requestDTO.getScheduleId() != null, ScheduleRequest::getScheduleId, requestDTO.getScheduleId())
                .eq(requestDTO.getRequestType() != null, ScheduleRequest::getRequestType, requestDTO.getRequestType())
                .eq(requestDTO.getStatus() != null, ScheduleRequest::getStatus, requestDTO.getStatus())
                .orderByDesc(ScheduleRequest::getCreateTime);

        IPage<ScheduleRequestDTO.ScheduleRequestListDTO> page = baseMapper.selectJoinPage(
                new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize()),
                ScheduleRequestDTO.ScheduleRequestListDTO.class,
                queryWrapper);

        return Result.success(new PageDTO<>(page));
    }

    @Override
    public Result<ScheduleRequestDTO.ScheduleRequestDetailDTO> getScheduleRequestById(Long requestId) {
        ScheduleRequest request = baseMapper.selectById(requestId);
        if (request == null) {
            return Result.error("申请不存在");
        }

        MPJLambdaWrapper<ScheduleRequest> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper
                .select(ScheduleRequest::getRequestId,
                        ScheduleRequest::getDoctorUserId,
                        ScheduleRequest::getScheduleId,
                        ScheduleRequest::getRequestType,
                        ScheduleRequest::getStatus,
                        ScheduleRequest::getTargetDate,
                        ScheduleRequest::getTargetSlotPeriod,
                        ScheduleRequest::getTargetSlotType,
                        ScheduleRequest::getExtraSlots,
                        ScheduleRequest::getReason,
                        ScheduleRequest::getApproverUserId,
                        ScheduleRequest::getApproveRemark,
                        ScheduleRequest::getApproveTime,
                        ScheduleRequest::getCreateTime,
                        ScheduleRequest::getUpdateTime)
                .leftJoin(User.class, User::getUserId, ScheduleRequest::getDoctorUserId)
                .selectAs(User::getName, "doctorName")
                .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, ScheduleRequest::getScheduleId)
                .selectAs(DoctorSchedule::getScheduleDate, "scheduleDate")
                .selectAs(DoctorSchedule::getSlotPeriod, "slotPeriod")
                .selectAs(DoctorSchedule::getSlotType, "slotType")
                .eq(ScheduleRequest::getRequestId, requestId);

        ScheduleRequestDTO.ScheduleRequestDetailDTO detailDTO = baseMapper.selectJoinOne(
                ScheduleRequestDTO.ScheduleRequestDetailDTO.class, queryWrapper);

        if (detailDTO == null) {
            return Result.error("申请不存在");
        }

        return Result.success(detailDTO);
    }

    @Override
    public Result<String> updateScheduleRequestById(Long requestId, ScheduleRequestDTO.ScheduleRequestUpdateDTO updateDTO) {
        ScheduleRequest request = baseMapper.selectById(requestId);
        if (request == null ) {
            return Result.error("申请不存在");
        }

        if (!ScheduleRequestStatusEnum.PENDING.getCode().equals(request.getStatus())) {
            return Result.error("仅待审批申请支持更新");
        }

        updateDTO.updateRequest(request);

        Result<String> validate = validateTypeParams(request.getRequestType(),
                request.getTargetDate(),
                request.getTargetSlotPeriod(),
                request.getExtraSlots());
        if (validate.getCode() != 200) {
            return Result.error(validate.getMsg());
        }

        baseMapper.updateById(request);
        return Result.success("更新成功");
    }

    @Override
    public Result<String> deleteScheduleRequestById(Long requestId) {
        ScheduleRequest request = baseMapper.selectById(requestId);
        if (request == null) {
            return Result.error("申请不存在");
        }

        if (!ScheduleRequestStatusEnum.PENDING.getCode().equals(request.getStatus())
                && !ScheduleRequestStatusEnum.REJECTED.getCode().equals(request.getStatus())) {
            return Result.error("仅待审批或已拒绝的申请可以删除");
        }

        baseMapper.deleteById(requestId);
        return Result.success("删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> handleScheduleRequest(Long requestId, ScheduleRequestDTO.ScheduleRequestHandleDTO handleDTO) {
        ScheduleRequest request = baseMapper.selectById(requestId);
        if (request == null) {
            return Result.error("申请不存在");
        }

        if (!ScheduleRequestStatusEnum.PENDING.getCode().equals(request.getStatus())) {
            return Result.error("该申请已处理");
        }

        DoctorSchedule schedule = scheduleMapper.selectById(request.getScheduleId());
        if (schedule == null) {
            return Result.error("关联排班不存在");
        }

        if (!schedule.getDoctorUserId().equals(request.getDoctorUserId())) {
            return Result.error("申请人与排班信息不一致");
        }

        if (Boolean.TRUE.equals(handleDTO.getApproved())) {
            Result<String> applyResult = applyRequestEffect(request, schedule);
            if (applyResult.getCode() != 200) {
                return applyResult;
            }
            request.setStatus(ScheduleRequestStatusEnum.APPROVED.getCode());
        } else {
            request.setStatus(ScheduleRequestStatusEnum.REJECTED.getCode());
        }

        request.setApproveRemark(handleDTO.getApproveRemark());
        request.setApproveTime(LocalDateTime.now());

        LoginUser loginUser = null;
        try {
            loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e) {
            log.warn("获取审批人信息失败: {}", e.getMessage());
        }
        if (loginUser != null && loginUser.getUser() != null) {
            request.setApproverUserId(loginUser.getUser().getUserId());
        }

        baseMapper.updateById(request);
        return Result.success("处理成功");
    }

    @Override
    public Result<String> cancelScheduleRequest(Long requestId) {
        ScheduleRequest request = baseMapper.selectById(requestId);
        if (request == null) {
            return Result.error("申请不存在");
        }

        if (!ScheduleRequestStatusEnum.PENDING.getCode().equals(request.getStatus())) {
            return Result.error("只有待审批的申请可以撤销");
        }

        LoginUser loginUser = null;
        try {
            loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e) {
            log.warn("获取撤销人信息失败: {}", e.getMessage());
        }
        if (loginUser != null && loginUser.getUser() != null) {
            Long currentUserId = loginUser.getUser().getUserId();
            if (!currentUserId.equals(request.getDoctorUserId())) {
                return Result.error("无权撤销其他医生的申请");
            }
        }

        request.setStatus(ScheduleRequestStatusEnum.CANCELLED.getCode());
        baseMapper.updateById(request);
        return Result.success("撤销成功");
    }

    private Result<String> applyRequestEffect(ScheduleRequest request, DoctorSchedule schedule) {
        if (ScheduleRequestTypeEnum.SHIFT_CHANGE.getCode().equals(request.getRequestType())) {
            return applyShiftChange(request, schedule);
        }

        if (ScheduleRequestTypeEnum.LEAVE.getCode().equals(request.getRequestType())) {
            return applyLeave(schedule);
        }

        if (ScheduleRequestTypeEnum.EXTRA_SLOT.getCode().equals(request.getRequestType())) {
            return applyExtraSlot(request, schedule);
        }

        return Result.error("不支持的申请类型");
    }

    private Result<String> applyShiftChange(ScheduleRequest request, DoctorSchedule schedule) {
        if (request.getTargetDate() == null || request.getTargetSlotPeriod() == null) {
            return Result.error("调班申请缺少目标日期或时段");
        }

        if (request.getExtraSlots() != null && request.getExtraSlots() < 0) {
            return Result.error("加号数量必须大于0");
        }

        if (request.getTargetSlotType() == null || request.getTargetSlotType() < 1 || request.getTargetSlotType() > 3) {
            return Result.error("目标号别不合法");
        }

        if (request.getTargetSlotPeriod() < 1 || request.getTargetSlotPeriod() > 12) {
            return Result.error("目标时段不合法");
        }

        LambdaQueryWrapper<Appointment> appointmentWrapper = new LambdaQueryWrapper<>();
        appointmentWrapper.eq(Appointment::getScheduleId, schedule.getScheduleId())
                .in(Appointment::getStatus, 1, 2, 3);
        Long appointmentCount = appointmentMapper.selectCount(appointmentWrapper);
        if (appointmentCount > 0) {
            return Result.error("该排班存在未完成的预约，无法调班");
        }

        LambdaQueryWrapper<DoctorSchedule> conflictWrapper = new LambdaQueryWrapper<>();
        conflictWrapper.eq(DoctorSchedule::getDoctorUserId, schedule.getDoctorUserId())
                .eq(DoctorSchedule::getScheduleDate, request.getTargetDate())
                .eq(DoctorSchedule::getSlotPeriod, request.getTargetSlotPeriod())
                .ne(DoctorSchedule::getScheduleId, schedule.getScheduleId());
        Long conflictCount = scheduleMapper.selectCount(conflictWrapper);
        if (conflictCount > 0) {
            return Result.error("目标日期与时段已存在排班，无法调班");
        }

        schedule.setScheduleDate(request.getTargetDate());
        schedule.setSlotPeriod(request.getTargetSlotPeriod());
        if (request.getTargetSlotType() != null) {
            schedule.setSlotType(request.getTargetSlotType());
        }
        if (request.getExtraSlots() != null) {
            schedule.setTotalSlots(schedule.getTotalSlots() + request.getExtraSlots());
            schedule.setAvailableSlots(schedule.getAvailableSlots() + request.getExtraSlots());
        }
        schedule.setUpdateTime(LocalDateTime.now());

        scheduleMapper.updateById(schedule);
        return Result.success("调班已生效");
    }

    private Result<String> applyLeave(DoctorSchedule schedule) {
        LambdaQueryWrapper<Appointment> appointmentWrapper = new LambdaQueryWrapper<>();
        appointmentWrapper.eq(Appointment::getScheduleId, schedule.getScheduleId())
                .in(Appointment::getStatus, 1, 2, 3);
        Long appointmentCount = appointmentMapper.selectCount(appointmentWrapper);
        if (appointmentCount > 0) {
            return Result.error("该排班存在未完成的预约，无法休假");
        }

        schedule.setStatus(0);
        schedule.setAvailableSlots(0);
        schedule.setUpdateTime(LocalDateTime.now());
        scheduleMapper.updateById(schedule);
        return Result.success("休假已生效");
    }

    private Result<String> applyExtraSlot(ScheduleRequest request, DoctorSchedule schedule) {
        if (request.getExtraSlots() == null || request.getExtraSlots() < 1) {
            return Result.error("加号数量必须大于0");
        }

        Integer newTotal = (schedule.getTotalSlots() == null ? 0 : schedule.getTotalSlots()) + request.getExtraSlots();
        Integer newAvailable = (schedule.getAvailableSlots() == null ? 0 : schedule.getAvailableSlots()) + request.getExtraSlots();

        schedule.setTotalSlots(newTotal);
        schedule.setAvailableSlots(newAvailable);
        if (schedule.getStatus() != null && schedule.getStatus() == 0) {
            schedule.setStatus(1);
        }
        schedule.setUpdateTime(LocalDateTime.now());
        scheduleMapper.updateById(schedule);
        return Result.success("加号已生效");
    }

    private boolean isDoctorUser(Long userId) {
        MPJLambdaWrapper<UserRole> roleQueryWrapper = new MPJLambdaWrapper<>();
        roleQueryWrapper.select(Role::getType)
                .leftJoin(Role.class, Role::getRoleId, UserRole::getRoleId)
                .eq(UserRole::getUserId, userId);

        Role userRole = userRoleMapper.selectJoinOne(Role.class, roleQueryWrapper);
        return userRole != null && RoleTypeEnum.DOCTOR.getCode().equals(userRole.getType());
    }

    private Result<String> validateTypeParams(Integer requestType, LocalDate targetDate, Integer targetSlotPeriod, Integer extraSlots) {
        if (ScheduleRequestTypeEnum.SHIFT_CHANGE.getCode().equals(requestType)) {
            if (targetDate == null || targetSlotPeriod == null) {
                return Result.error("调班需要提供目标日期和时段");
            }
        }

        if (ScheduleRequestTypeEnum.EXTRA_SLOT.getCode().equals(requestType)) {
            if (extraSlots == null || extraSlots < 1) {
                return Result.error("请提供有效的加号数量");
            }
        }

        if (ScheduleRequestTypeEnum.LEAVE.getCode().equals(requestType)) {
            return Result.success("ok");
        }

        if (!ScheduleRequestTypeEnum.SHIFT_CHANGE.getCode().equals(requestType)
                && !ScheduleRequestTypeEnum.LEAVE.getCode().equals(requestType)
                && !ScheduleRequestTypeEnum.EXTRA_SLOT.getCode().equals(requestType)) {
            return Result.error("不支持的申请类型");
        }

        return Result.success("ok");
    }
}
