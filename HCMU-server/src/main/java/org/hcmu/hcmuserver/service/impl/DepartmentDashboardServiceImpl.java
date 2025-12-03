package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.DepartmentLoadStatusEnum;
import org.hcmu.hcmucommon.enumeration.StatisticsTimeRangeEnum;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.DepartmentDashboardDTO;
import org.hcmu.hcmupojo.entity.Appointment;
import org.hcmu.hcmupojo.entity.Department;
import org.hcmu.hcmupojo.entity.DoctorProfile;
import org.hcmu.hcmupojo.entity.DoctorSchedule;
import org.hcmu.hcmupojo.vo.DepartmentDashboardVO;
import org.hcmu.hcmuserver.mapper.appointment.AppointmentMapper;
import org.hcmu.hcmuserver.mapper.department.DepartmentMapper;
import org.hcmu.hcmuserver.mapper.schedule.ScheduleMapper;
import org.hcmu.hcmuserver.service.DepartmentDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 科室可视化大屏服务实现
 */
@Service
@Slf4j
public class DepartmentDashboardServiceImpl implements DepartmentDashboardService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public Result<DepartmentDashboardVO.LoadStatisticsVO> getDepartmentLoadStatistics() {

        LambdaQueryWrapper<Department> departmentWrapper = new LambdaQueryWrapper<>();
        departmentWrapper.ne(Department::getDepartmentId, 0);
        List<Department> departments = departmentMapper.selectList(departmentWrapper);

        int totalDepartments = departments.size();
        int highLoadCount = 0;
        int mediumLoadCount = 0;
        int lowLoadCount = 0;
        int idleCount = 0;

        List<DepartmentDashboardVO.DepartmentInfoVO> highLoadList = new ArrayList<>();
        List<DepartmentDashboardVO.DepartmentInfoVO> mediumLoadList = new ArrayList<>();
        List<DepartmentDashboardVO.DepartmentInfoVO> lowLoadList = new ArrayList<>();
        List<DepartmentDashboardVO.DepartmentInfoVO> idleList = new ArrayList<>();

        for (Department department : departments) {
            Long departmentId = department.getDepartmentId();
            String departmentName = department.getName();

            MPJLambdaWrapper<DoctorSchedule> scheduleWrapper = new MPJLambdaWrapper<>();
            scheduleWrapper
                    .selectSum(DoctorSchedule::getTotalSlots, "totalSlots")
                    .selectSum(DoctorSchedule::getAvailableSlots, "availableSlots")
                    .leftJoin(DoctorProfile.class, DoctorProfile::getUserId, DoctorSchedule::getDoctorUserId)
                    .eq(DoctorProfile::getDepartmentId, departmentId);

            Map<String, Object> resultMap = scheduleMapper.selectJoinMaps(scheduleWrapper).stream()
                    .findFirst()
                    .orElse(new HashMap<>());

            Object totalSlotsObj = resultMap.get("totalSlots");
            Object availableSlotsObj = resultMap.get("availableSlots");

            DepartmentDashboardVO.DepartmentInfoVO simpleVO = DepartmentDashboardVO.DepartmentInfoVO.builder()
                    .departmentId(departmentId)
                    .departmentName(departmentName)
                    .build();

            if (totalSlotsObj == null || availableSlotsObj == null) {
                idleCount++;
                idleList.add(simpleVO);
                continue;
            }

            int totalSlots = ((Number) totalSlotsObj).intValue();
            int availableSlots = ((Number) availableSlotsObj).intValue();

            if (totalSlots == 0) {
                idleCount++;
                idleList.add(simpleVO);
                continue;
            }

            double availabilityRate = (double) availableSlots / totalSlots;

            DepartmentLoadStatusEnum loadStatus = DepartmentLoadStatusEnum.getByAvailabilityRate(availabilityRate);

            switch (loadStatus) {
                case HIGH:
                    highLoadCount++;
                    highLoadList.add(simpleVO);
                    break;
                case MEDIUM:
                    mediumLoadCount++;
                    mediumLoadList.add(simpleVO);
                    break;
                case LOW:
                    lowLoadCount++;
                    lowLoadList.add(simpleVO);
                    break;
                case IDLE:
                    idleCount++;
                    idleList.add(simpleVO);
                    break;
            }
        }

        DepartmentDashboardVO.LoadStatisticsVO result = DepartmentDashboardVO.LoadStatisticsVO.builder()
                .totalDepartments(totalDepartments)
                .highLoadDepartments(highLoadCount)
                .mediumLoadDepartments(mediumLoadCount)
                .lowLoadDepartments(lowLoadCount)
                .idleDepartments(idleCount)
                .highLoadDepartmentList(highLoadList)
                .mediumLoadDepartmentList(mediumLoadList)
                .lowLoadDepartmentList(lowLoadList)
                .idleDepartmentList(idleList)
                .build();

        return Result.success(result);
    }

    @Override
    public Result<DepartmentDashboardVO.AppointmentRankVO> getDepartmentAppointmentRank(DepartmentDashboardDTO.AppointmentRankDTO requestDTO) {

        StatisticsTimeRangeEnum timeRange = requestDTO.getTimeRange();

        MPJLambdaWrapper<Appointment> wrapper = new MPJLambdaWrapper<>();
        wrapper
                .select(DoctorProfile::getDepartmentId)
                .selectCount(Appointment::getAppointmentId, "appointmentCount")
                .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
                .leftJoin(DoctorProfile.class, DoctorProfile::getUserId, DoctorSchedule::getDoctorUserId);


        if (timeRange != StatisticsTimeRangeEnum.ALL && timeRange.getDays() != null) {
            LocalDateTime startTime = LocalDateTime.now().minusDays(timeRange.getDays());
            wrapper.ge(Appointment::getCreateTime, startTime);
        }


        wrapper.groupBy(DoctorProfile::getDepartmentId)
                .orderByDesc("appointmentCount")
                .last("LIMIT " + requestDTO.getLimit());

        List<Map<String, Object>> resultMaps = appointmentMapper.selectJoinMaps(wrapper);

        log.info("查询到的数据条数: {}", resultMaps.size());
        if (!resultMaps.isEmpty()) {
            log.info("第一条数据的keys: {}", resultMaps.get(0).keySet());
            log.info("第一条数据: {}", resultMaps.get(0));
        }

        List<DepartmentDashboardVO.AppointmentRankItemVO> rankList = new ArrayList<>();

        int rank = 1;
        for (Map<String, Object> map : resultMaps) {

            Object departmentIdObj = map.get("department_id");
            if (departmentIdObj == null) {
                departmentIdObj = map.get("departmentId");
            }

            Object appointmentCountObj = map.get("appointmentCount");
            if (appointmentCountObj == null) {
                appointmentCountObj = map.get("appointment_count");
            }

            log.info("处理记录 - departmentIdObj: {}, appointmentCountObj: {}", departmentIdObj, appointmentCountObj);

            if (departmentIdObj == null) {
                log.warn("department_id 为空，跳过该记录");
                continue;
            }

            Long departmentId = ((Number) departmentIdObj).longValue();
            Long appointmentCount = appointmentCountObj != null ? ((Number) appointmentCountObj).longValue() : 0L;

            // 查询科室名称
            Department department = departmentMapper.selectById(departmentId);
            if (department == null || department.getIsDeleted() == 1) {
                log.warn("科室不存在或已删除，departmentId: {}", departmentId);
                continue;
            }

            DepartmentDashboardVO.AppointmentRankItemVO item = DepartmentDashboardVO.AppointmentRankItemVO.builder()
                    .rank(rank++)
                    .departmentId(departmentId)
                    .departmentName(department.getName())
                    .appointmentCount(appointmentCount)
                    .build();

            rankList.add(item);
        }

        log.info("最终返回的排行榜数量: {}", rankList.size());

        DepartmentDashboardVO.AppointmentRankVO result = DepartmentDashboardVO.AppointmentRankVO.builder()
                .rankList(rankList)
                .build();

        return Result.success(result);
    }
}