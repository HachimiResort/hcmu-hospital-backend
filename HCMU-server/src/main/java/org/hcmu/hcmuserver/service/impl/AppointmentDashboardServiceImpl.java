package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.PeriodEnum;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
                .in(Appointment::getStatus, 2, 3, 4, 6);
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

    @Override
    public Result<AppointmentDashboardVO.AppointmentTrendVO> getAppointmentTrend(AppointmentDashboardDTO.AppointmentTrendDTO requestDTO) {

        StatisticsTimeRangeEnum timeRange = requestDTO.getTimeRange();
        List<AppointmentDashboardVO.TrendDataPoint> trendData = new ArrayList<>();

        switch (timeRange) {
            case DAY:
                trendData = getTrendDataForDay();
                break;
            case WEEK:
                trendData = getTrendDataForWeek();
                break;
            case MONTH:
                trendData = getTrendDataForMonth();
                break;
            case ALL:
                trendData = getTrendDataForAll();
                break;
        }

        AppointmentDashboardVO.AppointmentTrendVO result = AppointmentDashboardVO.AppointmentTrendVO.builder()
                .trendData(trendData)
                .build();

        return Result.success(result);
    }

    private List<AppointmentDashboardVO.TrendDataPoint> getTrendDataForDay() {
        List<AppointmentDashboardVO.TrendDataPoint> trendData = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (PeriodEnum period : PeriodEnum.values()) {
            MPJLambdaWrapper<Appointment> wrapper = new MPJLambdaWrapper<>();
            wrapper.selectCount(Appointment::getAppointmentId, "count")
                    .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                    .eq(DoctorSchedule::getScheduleDate, today)
                    .eq(DoctorSchedule::getSlotPeriod, period.getCode());

            List<Map<String, Object>> resultMaps = appointmentMapper.selectJoinMaps(wrapper);
            Long count = 0L;
            if (!resultMaps.isEmpty() && resultMaps.get(0) != null) {
                Object countObj = resultMaps.get(0).get("count");
                if (countObj != null) {
                    count = ((Number) countObj).longValue();
                }
            }

            trendData.add(AppointmentDashboardVO.TrendDataPoint.builder()
                    .count(count)
                    .periodLabel(period.getDesc())
                    .build());
        }

        return trendData;
    }

    private List<AppointmentDashboardVO.TrendDataPoint> getTrendDataForWeek() {
        List<AppointmentDashboardVO.TrendDataPoint> trendData = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            MPJLambdaWrapper<Appointment> wrapper = new MPJLambdaWrapper<>();
            wrapper.selectCount(Appointment::getAppointmentId, "count")
                    .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                    .eq(DoctorSchedule::getScheduleDate, date);

            List<Map<String, Object>> resultMaps = appointmentMapper.selectJoinMaps(wrapper);
            Long count = 0L;
            if (!resultMaps.isEmpty() && resultMaps.get(0) != null) {
                Object countObj = resultMaps.get(0).get("count");
                if (countObj != null) {
                    count = ((Number) countObj).longValue();
                }
            }

            trendData.add(AppointmentDashboardVO.TrendDataPoint.builder()
                    .count(count)
                    .periodLabel(date.format(formatter))
                    .build());
        }

        return trendData;
    }

    private List<AppointmentDashboardVO.TrendDataPoint> getTrendDataForMonth() {
        List<AppointmentDashboardVO.TrendDataPoint> trendData = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 9; i >= 0; i--) {
            LocalDate startDate = today.minusDays(i * 3 + 2);
            LocalDate endDate = today.minusDays(i * 3);

            MPJLambdaWrapper<Appointment> wrapper = new MPJLambdaWrapper<>();
            wrapper.selectCount(Appointment::getAppointmentId, "count")
                    .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                    .ge(DoctorSchedule::getScheduleDate, startDate)
                    .le(DoctorSchedule::getScheduleDate, endDate);

            List<Map<String, Object>> resultMaps = appointmentMapper.selectJoinMaps(wrapper);
            Long count = 0L;
            if (!resultMaps.isEmpty() && resultMaps.get(0) != null) {
                Object countObj = resultMaps.get(0).get("count");
                if (countObj != null) {
                    count = ((Number) countObj).longValue();
                }
            }

            trendData.add(AppointmentDashboardVO.TrendDataPoint.builder()
                    .count(count)
                    .periodLabel(startDate.format(formatter))
                    .build());
        }

        return trendData;
    }

    private List<AppointmentDashboardVO.TrendDataPoint> getTrendDataForAll() {
        List<AppointmentDashboardVO.TrendDataPoint> trendData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 查询最早的预约记录
        LambdaQueryWrapper<Appointment> firstWrapper = new LambdaQueryWrapper<>();
        firstWrapper.orderByAsc(Appointment::getCreateTime).last("LIMIT 1");
        Appointment firstAppointment = appointmentMapper.selectOne(firstWrapper);

        // 查询最晚的预约记录
        LambdaQueryWrapper<Appointment> lastWrapper = new LambdaQueryWrapper<>();
        lastWrapper.orderByDesc(Appointment::getCreateTime).last("LIMIT 1");
        Appointment lastAppointment = appointmentMapper.selectOne(lastWrapper);

        if (firstAppointment == null || lastAppointment == null) {
            for (int i = 0; i < 10; i++) {
                trendData.add(AppointmentDashboardVO.TrendDataPoint.builder()
                        .count(0L)
                        .periodLabel(LocalDate.now().minusDays(i * 3).format(formatter))
                        .build());
            }
            return trendData;
        }

        LocalDateTime firstTime = firstAppointment.getCreateTime();
        LocalDateTime lastTime = lastAppointment.getCreateTime();

        long totalMillis = java.time.Duration.between(firstTime, lastTime).toMillis();
        long millisPerPeriod = totalMillis / 10;

        if (millisPerPeriod < 86400000) {
            millisPerPeriod = 86400000;
        }

        for (int i = 0; i < 10; i++) {
            LocalDateTime startTime = firstTime.plusNanos(i * millisPerPeriod * 1000000);
            LocalDateTime endTime = (i == 9) ? lastTime : firstTime.plusNanos((i + 1) * millisPerPeriod * 1000000);

            LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(Appointment::getCreateTime, startTime)
                    .lt(Appointment::getCreateTime, endTime);

            Long count = appointmentMapper.selectCount(wrapper);

            trendData.add(AppointmentDashboardVO.TrendDataPoint.builder()
                    .count(count)
                    .periodLabel(startTime.toLocalDate().format(formatter))
                    .build());
        }

        return trendData;
    }
}
