package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;

import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.RoleTypeEnum;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.AppointmentDTO;
import org.hcmu.hcmupojo.dto.PatientProfileDTO;
import org.hcmu.hcmupojo.LoginUser;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.entity.*;
import org.hcmu.hcmupojo.entity.relation.UserRole;
import org.hcmu.hcmuserver.mapper.appointment.AppointmentMapper;
import org.hcmu.hcmuserver.mapper.patientprofile.PatientProfileMapper;
import org.hcmu.hcmuserver.mapper.user.UserRoleMapper;
import org.hcmu.hcmuserver.service.AppointmentService;
import org.hcmu.hcmuserver.service.PatientProfileService;
import org.hcmu.hcmuserver.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AppointmentServiceImpl extends MPJBaseServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    @Autowired
    private UserService userService;

    @Override
    public Result<PageDTO<AppointmentDTO.AppointmentListDTO>> getAppointments(AppointmentDTO.AppointmentGetRequestDTO requestDTO) {
        MPJLambdaWrapper<Appointment> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.selectAll(Appointment.class)
                .leftJoin(User.class, User::getUserId, Appointment::getPatientUserId)
                .selectAs(User::getUserName, "patientUserName")
                .selectAs(User::getName, "patientName")
                .selectAs(User::getPhone, "patientPhone")
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
                // 关联用户表获取患者信息
                .leftJoin(User.class, User::getUserId, Appointment::getPatientUserId)
                .selectAs(User::getUserName, "patientUserName")
                .selectAs(User::getName, "patientName")
                .selectAs(User::getPhone, "patientPhone")
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

}
