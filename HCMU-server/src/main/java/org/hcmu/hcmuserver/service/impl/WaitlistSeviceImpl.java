package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.WaitlistDTO;
import org.hcmu.hcmupojo.entity.User;
import org.hcmu.hcmupojo.entity.Waitlist;
import org.hcmu.hcmuserver.mapper.Waitlist.WaitlistMapper;
import org.hcmu.hcmuserver.service.WaitlistService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WaitlistSeviceImpl extends MPJBaseServiceImpl<WaitlistMapper, Waitlist> implements WaitlistService {
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
                .eq(requestDTO.getPatientUserId() != null, Waitlist::getPatientUserId, requestDTO.getPatientUserId())
                .eq(requestDTO.getScheduleId() != null, Waitlist::getScheduleId, requestDTO.getScheduleId())
                .eq(requestDTO.getStatus() != null, Waitlist::getStatus, requestDTO.getStatus())
                .eq(requestDTO.getIsDeleted() != null, Waitlist::getIsDeleted, requestDTO.getIsDeleted())
                .orderByDesc(Waitlist::getCreateTime);

        // 默认查询未删除的记录
        if (requestDTO.getIsDeleted() == null) {
            queryWrapper.eq(Waitlist::getIsDeleted, 0);
        }

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
                .eq(Waitlist::getWaitlistId, waitlistId)
                .eq(Waitlist::getIsDeleted, 0);

        WaitlistDTO.WaitlistDetailDTO detailDTO = baseMapper.selectJoinOne(
                WaitlistDTO.WaitlistDetailDTO.class, queryWrapper);

        if (detailDTO == null) {
            return Result.error("候诊记录不存在或已被删除");
        }

        return Result.success(detailDTO);
    }

}
