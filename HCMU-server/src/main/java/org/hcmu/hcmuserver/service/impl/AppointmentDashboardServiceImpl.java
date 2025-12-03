package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.StatisticsTimeRangeEnum;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.AppointmentDashboardDTO;
import org.hcmu.hcmupojo.entity.Appointment;
import org.hcmu.hcmupojo.entity.DoctorSchedule;
import org.hcmu.hcmupojo.vo.AppointmentDashboardVO;
import org.hcmu.hcmuserver.mapper.appointment.AppointmentMapper;
import org.hcmu.hcmuserver.service.AppointmentDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 号源可视化大屏服务实现
 */
@Service
@Slf4j
public class AppointmentDashboardServiceImpl implements AppointmentDashboardService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public Result<AppointmentDashboardVO.AppointmentStatisticsVO> getAppointmentStatistics(AppointmentDashboardDTO.AppointmentStatisticsDTO requestDTO) {

        StatisticsTimeRangeEnum timeRange = requestDTO.getTimeRange();
        LocalDateTime startTime = null;
        if (timeRange != StatisticsTimeRangeEnum.ALL && timeRange.getDays() != null) {
            startTime = LocalDateTime.now().minusDays(timeRange.getDays());
        }

        LambdaQueryWrapper<Appointment> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(Appointment::getStatus, 1);
        if (startTime != null) {
            wrapper1.ge(Appointment::getCreateTime, startTime);
        }
        Long pendingPaymentCount = appointmentMapper.selectCount(wrapper1);

        LambdaQueryWrapper<Appointment> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(Appointment::getStatus, 2);
        if (startTime != null) {
            wrapper2.ge(Appointment::getCreateTime, startTime);
        }
        Long bookedCount = appointmentMapper.selectCount(wrapper2);

        LambdaQueryWrapper<Appointment> wrapper4 = new LambdaQueryWrapper<>();
        wrapper4.eq(Appointment::getStatus, 4);
        if (startTime != null) {
            wrapper4.ge(Appointment::getCreateTime, startTime);
        }
        Long completedCount = appointmentMapper.selectCount(wrapper4);

        LambdaQueryWrapper<Appointment> wrapper5 = new LambdaQueryWrapper<>();
        wrapper5.eq(Appointment::getStatus, 5);
        if (startTime != null) {
            wrapper5.ge(Appointment::getCreateTime, startTime);
        }
        Long cancelledCount = appointmentMapper.selectCount(wrapper5);

        LambdaQueryWrapper<Appointment> wrapper6 = new LambdaQueryWrapper<>();
        wrapper6.eq(Appointment::getStatus, 6);
        if (startTime != null) {
            wrapper6.ge(Appointment::getCreateTime, startTime);
        }
        Long noShowCount = appointmentMapper.selectCount(wrapper6);

        MPJLambdaWrapper<Appointment> revenueWrapper = new MPJLambdaWrapper<>();
        revenueWrapper.selectSum(DoctorSchedule::getFee, "totalFee")
                .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                .in(Appointment::getStatus, 2, 3, 4);
        if (startTime != null) {
            revenueWrapper.ge(Appointment::getCreateTime, startTime);
        }
        List<Map<String, Object>> resultMaps = appointmentMapper.selectJoinMaps(revenueWrapper);
        BigDecimal totalRevenue = BigDecimal.ZERO;
        if (!resultMaps.isEmpty() && resultMaps.get(0) != null) {
            Object totalFeeObj = resultMaps.get(0).get("totalFee");
            if (totalFeeObj != null) {
                totalRevenue = new BigDecimal(totalFeeObj.toString());
            }
        }

        AppointmentDashboardVO.AppointmentStatisticsVO result = AppointmentDashboardVO.AppointmentStatisticsVO.builder()
                .pendingPaymentCount(pendingPaymentCount)
                .bookedCount(bookedCount)
                .completedCount(completedCount)
                .cancelledCount(cancelledCount)
                .noShowCount(noShowCount)
                .totalRevenue(totalRevenue)
                .build();

        return Result.success(result);
    }
}
