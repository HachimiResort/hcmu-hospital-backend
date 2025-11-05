package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
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
import org.hcmu.hcmupojo.entity.Appointment;
import org.hcmu.hcmupojo.entity.PatientProfile;
import org.hcmu.hcmupojo.entity.Role;
import org.hcmu.hcmupojo.entity.User;
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

@Service
@Slf4j
public class AppointmentImpl extends MPJBaseServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    @Autowired
    private UserService userService;

    @Override
    public Result<PageDTO<AppointmentDTO.AppointmentListDTO>> getAppointments(AppointmentDTO.AppointmentGetRequsetDTO requestDTO) {
        MPJLambdaWrapper<Appointment> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(Appointment::getAppointmentId,
                        Appointment::getAppointmentNo,
                        Appointment::getPatientUserId,
                        Appointment::getScheduleId,
                        Appointment::getVisitNo,
                        Appointment::getStatus,
                        Appointment::getCreateTime)
                .leftJoin(User.class, User::getUserId, Appointment::getPatientUserId)
                .selectAs(User::getUserName, "patientUserName")
                .eq(requestDTO.getScheduleId() != null,
                        Appointment::getScheduleId, requestDTO.getScheduleId())
                .eq(requestDTO.getPatientUserId() != null,
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
    public Result<AppointmentDTO.AppointmentDetailDTO> getAppointmentById(Long appointmentId) {
        if (appointmentId == null || appointmentId <= 0) {
            return Result.error("预约ID不能为空");
        }

        // 构建查询条件
        MPJLambdaWrapper<Appointment> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(Appointment::getAppointmentId,
                        Appointment::getAppointmentNo,
                        Appointment::getPatientUserId,
                        Appointment::getScheduleId,
                        Appointment::getVisitNo,
                        Appointment::getStatus,
                        Appointment::getOriginalFee,
                        Appointment::getActualFee,
                        Appointment::getPaymentTime,
                        Appointment::getCancellationTime,
                        Appointment::getCancellationReason,
                        Appointment::getCreateTime,
                        Appointment::getUpdateTime)
                // 关联用户表获取患者信息
                .leftJoin(User.class, User::getUserId, Appointment::getPatientUserId)
                .selectAs(User::getUserName, "patientUserName")
                .selectAs(User::getPhone, "patientPhone")
                .eq(Appointment::getAppointmentId, appointmentId)
                .eq(Appointment::getIsDeleted, 0);  // 只查询未删除的记录

        AppointmentDTO.AppointmentDetailDTO detailDTO = baseMapper.selectJoinOne(
                AppointmentDTO.AppointmentDetailDTO.class,
                queryWrapper
        );

        if (detailDTO == null) {
            return Result.error("预约记录不存在或已被删除");
        }

        return Result.success(detailDTO);
    }

}
