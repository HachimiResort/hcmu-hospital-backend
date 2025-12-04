package org.hcmu.hcmuserver.service.impl;

import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.StatisticsTimeRangeEnum;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.DoctorDashboardDTO;
import org.hcmu.hcmupojo.entity.Appointment;
import org.hcmu.hcmupojo.entity.DoctorSchedule;
import org.hcmu.hcmupojo.entity.User;
import org.hcmu.hcmupojo.vo.DoctorDashboardVO;
import org.hcmu.hcmuserver.mapper.appointment.AppointmentMapper;
import org.hcmu.hcmuserver.service.DoctorDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 医生大屏服务实现
 */
@Service
@Slf4j
public class DoctorDashboardServiceImpl implements DoctorDashboardService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public Result<DoctorDashboardVO.DoctorVisitRankVO> getDoctorVisitRank(DoctorDashboardDTO.DoctorVisitRankDTO requestDTO) {

        StatisticsTimeRangeEnum timeRange = requestDTO.getTimeRange();
        Integer limit = requestDTO.getLimit();

        LocalDateTime startTime = null;
        if (timeRange != StatisticsTimeRangeEnum.ALL && timeRange.getDays() != null) {
            startTime = LocalDateTime.now().minusDays(timeRange.getDays());
        }

        MPJLambdaWrapper<Appointment> wrapper = new MPJLambdaWrapper<>();
        wrapper.selectCount(Appointment::getAppointmentId, "visitCount")
                .select(DoctorSchedule::getDoctorUserId)
                .selectAs(User::getName, "doctorName")
                .innerJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                .innerJoin(User.class, User::getUserId, DoctorSchedule::getDoctorUserId)
                .eq(Appointment::getStatus, 4)
                .isNotNull(DoctorSchedule::getDoctorUserId);

        if (startTime != null) {
            wrapper.ge(Appointment::getCreateTime, startTime);
        }

        wrapper.groupBy(DoctorSchedule::getDoctorUserId)
                .orderByDesc("visitCount")
                .last("LIMIT " + limit);

        List<Map<String, Object>> resultMaps = appointmentMapper.selectJoinMaps(wrapper);

        List<DoctorDashboardVO.DoctorVisitRankItemVO> rankList = new ArrayList<>();
        int rank = 1;
        for (Map<String, Object> map : resultMaps) {
            Long doctorUserId = map.get("doctor_user_id") != null ? ((Number) map.get("doctor_user_id")).longValue() : null;
            String doctorName = (String) map.get("doctorName");
            Long visitCount = map.get("visitCount") != null ? ((Number) map.get("visitCount")).longValue() : 0L;

            DoctorDashboardVO.DoctorVisitRankItemVO item = DoctorDashboardVO.DoctorVisitRankItemVO.builder()
                    .rank(rank++)
                    .doctorUserId(doctorUserId)
                    .doctorName(doctorName)
                    .visitCount(visitCount)
                    .build();

            rankList.add(item);
        }

        DoctorDashboardVO.DoctorVisitRankVO result = DoctorDashboardVO.DoctorVisitRankVO.builder()
                .rankList(rankList)
                .build();

        return Result.success(result);
    }

    @Override
    public Result<DoctorDashboardVO.DoctorIncomeRankVO> getDoctorIncomeRank(DoctorDashboardDTO.DoctorIncomeRankDTO requestDTO) {

        StatisticsTimeRangeEnum timeRange = requestDTO.getTimeRange();
        Integer limit = requestDTO.getLimit();

        LocalDateTime startTime = null;
        if (timeRange != StatisticsTimeRangeEnum.ALL && timeRange.getDays() != null) {
            startTime = LocalDateTime.now().minusDays(timeRange.getDays());
        }

        MPJLambdaWrapper<Appointment> wrapper = new MPJLambdaWrapper<>();
        wrapper.selectSum(Appointment::getOriginalFee, "totalIncome")
                .select(DoctorSchedule::getDoctorUserId)
                .selectAs(User::getName, "doctorName")
                .innerJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                .innerJoin(User.class, User::getUserId, DoctorSchedule::getDoctorUserId)
                .in(Appointment::getStatus, 2, 3, 4, 6)
                .isNotNull(DoctorSchedule::getDoctorUserId);

        if (startTime != null) {
            wrapper.ge(Appointment::getCreateTime, startTime);
        }

        wrapper.groupBy(DoctorSchedule::getDoctorUserId)
                .orderByDesc("totalIncome")
                .last("LIMIT " + limit);

        List<Map<String, Object>> resultMaps = appointmentMapper.selectJoinMaps(wrapper);

        List<DoctorDashboardVO.DoctorIncomeRankItemVO> rankList = new ArrayList<>();
        int rank = 1;
        for (Map<String, Object> map : resultMaps) {
            Long doctorUserId = map.get("doctor_user_id") != null ? ((Number) map.get("doctor_user_id")).longValue() : null;
            String doctorName = (String) map.get("doctorName");
            BigDecimal totalIncome = BigDecimal.ZERO;
            if (map.get("totalIncome") != null) {
                totalIncome = new BigDecimal(map.get("totalIncome").toString());
            }

            DoctorDashboardVO.DoctorIncomeRankItemVO item = DoctorDashboardVO.DoctorIncomeRankItemVO.builder()
                    .rank(rank++)
                    .doctorUserId(doctorUserId)
                    .doctorName(doctorName)
                    .totalIncome(totalIncome)
                    .build();

            rankList.add(item);
        }

        DoctorDashboardVO.DoctorIncomeRankVO result = DoctorDashboardVO.DoctorIncomeRankVO.builder()
                .rankList(rankList)
                .build();

        return Result.success(result);
    }

    @Override
    public Result<DoctorDashboardVO.DoctorAppointmentRateVO> getDoctorAppointmentRate(DoctorDashboardDTO.DoctorAppointmentRateDTO requestDTO) {

        StatisticsTimeRangeEnum timeRange = requestDTO.getTimeRange();

        LocalDateTime startTime = null;
        if (timeRange != StatisticsTimeRangeEnum.ALL && timeRange.getDays() != null) {
            startTime = LocalDateTime.now().minusDays(timeRange.getDays());
        }

        // 查询每个医生的就诊、取消、爽约数量
        MPJLambdaWrapper<Appointment> wrapper = new MPJLambdaWrapper<>();
        wrapper.select(DoctorSchedule::getDoctorUserId)
                .selectAs(User::getName, "doctorName")
                .select("SUM(CASE WHEN t.status = 4 THEN 1 ELSE 0 END) AS completedCount")
                .select("SUM(CASE WHEN t.status = 5 THEN 1 ELSE 0 END) AS cancelledCount")
                .select("SUM(CASE WHEN t.status = 6 THEN 1 ELSE 0 END) AS noShowCount")
                .innerJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                .innerJoin(User.class, User::getUserId, DoctorSchedule::getDoctorUserId)
                .in(Appointment::getStatus, 4, 5, 6)
                .isNotNull(DoctorSchedule::getDoctorUserId);

        if (startTime != null) {
            wrapper.ge(Appointment::getCreateTime, startTime);
        }

        wrapper.groupBy(DoctorSchedule::getDoctorUserId);

        List<Map<String, Object>> resultMaps = appointmentMapper.selectJoinMaps(wrapper);

        List<DoctorDashboardVO.DoctorAppointmentRateItemVO> rateList = new ArrayList<>();
        for (Map<String, Object> map : resultMaps) {
            Long doctorUserId = map.get("doctor_user_id") != null ? ((Number) map.get("doctor_user_id")).longValue() : null;
            String doctorName = (String) map.get("doctorName");
            Long completedCount = map.get("completedCount") != null ? ((Number) map.get("completedCount")).longValue() : 0L;
            Long cancelledCount = map.get("cancelledCount") != null ? ((Number) map.get("cancelledCount")).longValue() : 0L;
            Long noShowCount = map.get("noShowCount") != null ? ((Number) map.get("noShowCount")).longValue() : 0L;

            Long totalCount = completedCount + cancelledCount + noShowCount;

            BigDecimal completedRate = BigDecimal.ZERO;
            BigDecimal cancelledRate = BigDecimal.ZERO;
            BigDecimal noShowRate = BigDecimal.ZERO;

            if (totalCount > 0) {
                completedRate = BigDecimal.valueOf(completedCount)
                        .divide(BigDecimal.valueOf(totalCount), 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                cancelledRate = BigDecimal.valueOf(cancelledCount)
                        .divide(BigDecimal.valueOf(totalCount), 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                noShowRate = BigDecimal.valueOf(noShowCount)
                        .divide(BigDecimal.valueOf(totalCount), 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            DoctorDashboardVO.DoctorAppointmentRateItemVO item = DoctorDashboardVO.DoctorAppointmentRateItemVO.builder()
                    .doctorUserId(doctorUserId)
                    .doctorName(doctorName)
                    .completedCount(completedCount)
                    .cancelledCount(cancelledCount)
                    .noShowCount(noShowCount)
                    .completedRate(completedRate)
                    .cancelledRate(cancelledRate)
                    .noShowRate(noShowRate)
                    .build();

            rateList.add(item);
        }

        DoctorDashboardVO.DoctorAppointmentRateVO result = DoctorDashboardVO.DoctorAppointmentRateVO.builder()
                .rateList(rateList)
                .build();

        return Result.success(result);
    }
}