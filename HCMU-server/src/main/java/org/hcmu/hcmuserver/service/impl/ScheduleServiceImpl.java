package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;

import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmucommon.enumeration.RoleTypeEnum;

import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.ScheduleDTO;
import org.hcmu.hcmupojo.entity.Schedule;
import org.hcmu.hcmupojo.entity.Role;
import org.hcmu.hcmupojo.entity.relation.UserRole;
import org.hcmu.hcmuserver.mapper.schedule.ScheduleMapper;
import org.hcmu.hcmuserver.mapper.user.UserRoleMapper;
import org.hcmu.hcmuserver.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ScheduleServiceImpl extends MPJBaseServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public Result<ScheduleDTO.ScheduleListDTO> createSchedule(ScheduleDTO.ScheduleCreateDTO createDTO) {

        // 校验同一医生在同一日期和时段是否已有排班
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getDoctorUserId, createDTO.getDoctorUserId())
                .eq(Schedule::getScheduleDate, createDTO.getScheduleDate())
                .eq(Schedule::getSlotType, createDTO.getSlotType())
                .eq(Schedule::getIsDeleted, 0);
        if (baseMapper.selectCount(wrapper) > 0) {
            return Result.error("该医生在此日期和时段已有排班");
        }

        Schedule schedule = new Schedule();
        schedule.setDoctorUserId(createDTO.getDoctorUserId());
        schedule.setDepartmentId(createDTO.getDepartmentId());
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
        result.setDepartmentId(schedule.getDepartmentId());
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
        MPJLambdaWrapper<Schedule> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(Schedule::getScheduleId, Schedule::getDoctorUserId, Schedule::getDepartmentId,
                        Schedule::getScheduleDate, Schedule::getSlotType, Schedule::getSlotPeriod,
                        Schedule::getTotalSlots, Schedule::getAvailableSlots, Schedule::getFee,
                        Schedule::getStatus, Schedule::getCreateTime)
                .eq(requestDTO.getDoctorUserId() != null, Schedule::getDoctorUserId, requestDTO.getDoctorUserId())
                .eq(requestDTO.getDepartmentId() != null, Schedule::getDepartmentId, requestDTO.getDepartmentId())
                .eq(requestDTO.getScheduleDate() != null, Schedule::getScheduleDate, requestDTO.getScheduleDate())
                .eq(requestDTO.getSlotType() != null, Schedule::getSlotType, requestDTO.getSlotType())
                .eq(requestDTO.getSlotPeriod() != null, Schedule::getSlotPeriod, requestDTO.getSlotPeriod())
                .eq(requestDTO.getStatus() != null, Schedule::getStatus, requestDTO.getStatus())
                .eq(requestDTO.getIsDeleted() != null, Schedule::getIsDeleted, requestDTO.getIsDeleted())
                .orderByDesc(Schedule::getCreateTime);

        IPage<ScheduleDTO.ScheduleListDTO> page = baseMapper.selectJoinPage(
                new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize()),
                ScheduleDTO.ScheduleListDTO.class,
                queryWrapper);

        return Result.success(new PageDTO<>(page));
    }

    @Override
    public Result<ScheduleDTO.ScheduleListDTO> findScheduleById(Long scheduleId) {
        Schedule schedule = baseMapper.selectById(scheduleId);
        if (schedule == null || schedule.getIsDeleted() == 1) {
            return Result.error("排班不存在");
        }

        ScheduleDTO.ScheduleListDTO dto = new ScheduleDTO.ScheduleListDTO();
        dto.setScheduleId(schedule.getScheduleId());
        dto.setDoctorUserId(schedule.getDoctorUserId());
        dto.setDepartmentId(schedule.getDepartmentId());
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
        Schedule schedule = baseMapper.selectById(scheduleId);
        if (schedule == null || schedule.getIsDeleted() == 1) {
            return Result.error("排班不存在");
        }

        if ((updateDTO.getDoctorUserId() != null && !updateDTO.getDoctorUserId().equals(schedule.getDoctorUserId())) ||
            (updateDTO.getScheduleDate() != null && !updateDTO.getScheduleDate().equals(schedule.getScheduleDate())) ||
            (updateDTO.getSlotType() != null && !updateDTO.getSlotType().equals(schedule.getSlotType()))) {
            
            Long doctorUserId = updateDTO.getDoctorUserId() != null ? updateDTO.getDoctorUserId() : schedule.getDoctorUserId();
            java.time.LocalDate scheduleDate = updateDTO.getScheduleDate() != null ? updateDTO.getScheduleDate() : schedule.getScheduleDate();
            Integer slotType = updateDTO.getSlotType() != null ? updateDTO.getSlotType() : schedule.getSlotType();
            
            LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Schedule::getDoctorUserId, doctorUserId)
                    .eq(Schedule::getScheduleDate, scheduleDate)
                    .eq(Schedule::getSlotType, slotType)
                    .eq(Schedule::getIsDeleted, 0)
                    .ne(Schedule::getScheduleId, scheduleId); // 排除当前记录
            if (baseMapper.selectCount(wrapper) > 0) {
                return Result.error("该医生在此日期和时段已有排班");
            }
        }

        updateDTO.updateSchedule(schedule);
        baseMapper.updateById(schedule);
        return Result.success("更新成功");
    }

    @Override
    public Result<String> deleteScheduleById(Long scheduleId) {
        Schedule schedule = baseMapper.selectById(scheduleId);
        if (schedule == null) {
            return Result.error("排班不存在");
        }

        //Todo: 如果有appointment，不能删除排班

        baseMapper.deleteById(scheduleId);
        return Result.success("删除成功");
    }

    @Override
    public Result<String> batchDeleteSchedules(List<Long> scheduleIds) {
        if (CollectionUtils.isEmpty(scheduleIds)) {
            return Result.error("请选择需要删除的排班");
        }

        // 校验排班是否存在
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Schedule::getScheduleId, scheduleIds);
        long existCount = baseMapper.selectCount(wrapper);
        if (existCount != scheduleIds.size()) {
            return Result.error("部分排班不存在");
        }

        //Todo: 如果有appointment，不能删除排班

        baseMapper.deleteBatchIds(scheduleIds);

        return Result.success("批量删除成功");
    }

    @Override
    public Result<String> copySchedule(ScheduleDTO.ScheduleCopyDTO copyDTO) {
        Long doctorUserId = copyDTO.getDoctorUserId();
        LocalDate targetDate = copyDTO.getTargetDate();
        
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
        LambdaQueryWrapper<Schedule> targetDateWrapper = new LambdaQueryWrapper<>();
        targetDateWrapper.eq(Schedule::getDoctorUserId, doctorUserId)
                .eq(Schedule::getScheduleDate, targetDate)
                .eq(Schedule::getIsDeleted, 0);
        
        Long existingScheduleCount = baseMapper.selectCount(targetDateWrapper);
        if (existingScheduleCount > 0) {
            return Result.error("目标日期已有排班，无法复制");
        }

        // 查找7天前的排班
        LocalDate sourceDate = targetDate.minusDays(7);
        LambdaQueryWrapper<Schedule> sourceDateWrapper = new LambdaQueryWrapper<>();
        sourceDateWrapper.eq(Schedule::getDoctorUserId, doctorUserId)
                .eq(Schedule::getScheduleDate, sourceDate)
                .eq(Schedule::getIsDeleted, 0);
        
        List<Schedule> sourceSchedules = baseMapper.selectList(sourceDateWrapper);
        if (sourceSchedules.isEmpty()) {
            return Result.error("7天前没有排班记录");
        }

        // 4. 复制排班记录
        for (Schedule sourceSchedule : sourceSchedules) {
            Schedule newSchedule = new Schedule();
            newSchedule.setDoctorUserId(sourceSchedule.getDoctorUserId());
            newSchedule.setDepartmentId(sourceSchedule.getDepartmentId());
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
}
