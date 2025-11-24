package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.WaitlistDTO;
import org.hcmu.hcmupojo.entity.DoctorSchedule;
import org.hcmu.hcmupojo.entity.Schedule;
import org.hcmu.hcmupojo.entity.User;
import org.hcmu.hcmupojo.entity.Waitlist;
import org.hcmu.hcmuserver.mapper.Waitlist.WaitlistMapper;
import org.hcmu.hcmuserver.mapper.schedule.ScheduleMapper;
import org.hcmu.hcmuserver.mapper.user.UserMapper;
import org.hcmu.hcmuserver.service.WaitlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WaitlistServiceImpl extends MPJBaseServiceImpl<WaitlistMapper, Waitlist> implements WaitlistService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<WaitlistDTO.WaitlistDetailDTO> createWaitlist(WaitlistDTO.WaitlistCreateDTO createDTO) {

        User user = userMapper.selectById(createDTO.getPatientUserId());
        if (user == null || user.getIsDeleted() == 1) {
            return Result.error("患者不存在");
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

        // 返回详情
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

}
