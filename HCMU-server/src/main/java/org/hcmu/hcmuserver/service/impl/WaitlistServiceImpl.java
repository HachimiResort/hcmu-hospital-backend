package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.RoleTypeEnum;
import org.hcmu.hcmucommon.enumeration.WaitListEnum;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.WaitlistDTO;
import org.hcmu.hcmupojo.entity.DoctorSchedule;
import org.hcmu.hcmupojo.entity.Role;
import org.hcmu.hcmupojo.entity.User;
import org.hcmu.hcmupojo.entity.Waitlist;
import org.hcmu.hcmupojo.entity.relation.UserRole;
import org.hcmu.hcmuserver.mapper.Waitlist.WaitlistMapper;
import org.hcmu.hcmuserver.mapper.role.RoleMapper;
import org.hcmu.hcmuserver.mapper.schedule.ScheduleMapper;
import org.hcmu.hcmuserver.mapper.user.UserMapper;
import org.hcmu.hcmuserver.mapper.user.UserRoleMapper;
import org.hcmu.hcmuserver.service.WaitlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public Result<WaitlistDTO.WaitlistDetailDTO> createWaitlist(WaitlistDTO.WaitlistCreateDTO createDTO) {

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
                .eq(Waitlist::getScheduleId, createDTO.getScheduleId());
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
    public Result<WaitlistDTO.WaitlistDetailDTO> getWaitlistById(Long waitlistId) {
        MPJLambdaWrapper<Waitlist> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(Waitlist::getWaitlistId,
                        Waitlist::getPatientUserId,
                        Waitlist::getScheduleId,
                        Waitlist::getStatus,
                        Waitlist::getNotifiedTime,
                        Waitlist::getLockExpireTime,
                        Waitlist::getCreateTime,
                        Waitlist::getUpdateTime)
                // 关联用户表获取患者详细信息
                .leftJoin(User.class, User::getUserId, Waitlist::getPatientUserId)
                .selectAs(User::getUserName, "patientUserName")
                .selectAs(User::getPhone, "patientPhone")
                .eq(Waitlist::getWaitlistId, waitlistId);


        WaitlistDTO.WaitlistDetailDTO detailDTO = baseMapper.selectJoinOne(
                WaitlistDTO.WaitlistDetailDTO.class, queryWrapper);

        if (detailDTO == null) {
            return Result.error("候诊记录不存在或已被删除");
        }

        return Result.success(detailDTO);
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

    @Override
    public Result<WaitlistDTO.WaitlistDetailDTO> patientJoinWaitlist(WaitlistDTO.PatientJoinDTO joinDTO) {

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

        LambdaQueryWrapper<Waitlist> duplicateWrapper = new LambdaQueryWrapper<>();
        duplicateWrapper.eq(Waitlist::getPatientUserId, joinDTO.getUserId())
                .eq(Waitlist::getScheduleId, joinDTO.getScheduleId());
        if (baseMapper.selectCount(duplicateWrapper) > 0) {
            return Result.error("您已在该排班的候补队列中");
        }

        Waitlist waitlist = Waitlist.builder()
                .patientUserId(joinDTO.getUserId())
                .scheduleId(joinDTO.getScheduleId())
                .status( WaitListEnum.WAITING.getCode())
                .build();

        baseMapper.insert(waitlist);

        return getWaitlistById(waitlist.getWaitlistId());
    }

}
