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
import org.hcmu.hcmuserver.mapper.doctorprofile.DoctorProfileMapper;
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
    private DoctorProfileMapper doctorProfileMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public Result<DepartmentDashboardVO.LoadStatisticsVO> getDepartmentLoadStatistics() {
        log.info("开始查询科室负荷统计数据");

        LambdaQueryWrapper<Department> departmentWrapper = new LambdaQueryWrapper<>();
        departmentWrapper.ne(Department::getDepartmentId, 0);
        List<Department> departments = departmentMapper.selectList(departmentWrapper);
        log.info("查询到科室总数: {}", departments.size());

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
            log.debug("正在处理科室: id={}, name={}", departmentId, departmentName);

            int totalSlots = 0;
            int availableSlots = 0;

            try {
                LambdaQueryWrapper<DoctorProfile> doctorWrapper = new LambdaQueryWrapper<>();
                doctorWrapper.select(DoctorProfile::getUserId)
                        .eq(DoctorProfile::getDepartmentId, departmentId);
                List<DoctorProfile> doctors = doctorProfileMapper.selectList(doctorWrapper);

                if (doctors == null || doctors.isEmpty()) {
                    log.debug("科室 {} 没有医生，标记为空闲", departmentName);
                    idleCount++;
                    idleList.add(buildDepartmentInfoVO(departmentId, departmentName));
                    continue;
                }

                List<Long> doctorIds = new ArrayList<>();
                for (DoctorProfile doctor : doctors) {
                    if (doctor.getUserId() != null) {
                        doctorIds.add(doctor.getUserId());
                    }
                }

                if (doctorIds.isEmpty()) {
                    log.debug("科室 {} 没有有效医生ID，标记为空闲", departmentName);
                    idleCount++;
                    idleList.add(buildDepartmentInfoVO(departmentId, departmentName));
                    continue;
                }
                
                LambdaQueryWrapper<DoctorSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
                scheduleWrapper.select(DoctorSchedule::getTotalSlots, DoctorSchedule::getAvailableSlots)
                        .in(DoctorSchedule::getDoctorUserId, doctorIds);
                List<DoctorSchedule> schedules = scheduleMapper.selectList(scheduleWrapper);

                if (schedules == null || schedules.isEmpty()) {
                    log.debug("科室 {} 没有排班信息，标记为空闲", departmentName);
                    idleCount++;
                    idleList.add(buildDepartmentInfoVO(departmentId, departmentName));
                    continue;
                }

                for (DoctorSchedule schedule : schedules) {
                    if (schedule.getTotalSlots() != null) {
                        totalSlots += schedule.getTotalSlots();
                    }
                    if (schedule.getAvailableSlots() != null) {
                        availableSlots += schedule.getAvailableSlots();
                    }
                }

                log.debug("科室 {} 槽位统计: totalSlots={}, availableSlots={}",
                        departmentName, totalSlots, availableSlots);

            } catch (Exception e) {
                log.error("查询科室负荷数据失败，departmentId: {}, departmentName: {}",
                        departmentId, departmentName, e);

                idleCount++;
                idleList.add(buildDepartmentInfoVO(departmentId, departmentName));
                continue;
            }

            DepartmentDashboardVO.DepartmentInfoVO simpleVO = buildDepartmentInfoVO(departmentId, departmentName);

            if (totalSlots == 0) {
                log.debug("科室 {} 总槽位为0，标记为空闲", departmentName);
                idleCount++;
                idleList.add(simpleVO);
                continue;
            }

            double availabilityRate = (double) availableSlots / totalSlots;
            log.debug("科室 {} 可用率: {}", departmentName, availabilityRate);

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

        log.info("科室负荷统计完成: 总数={}, 高负荷={}, 中负荷={}, 低负荷={}, 空闲={}",
                totalDepartments, highLoadCount, mediumLoadCount, lowLoadCount, idleCount);

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

    /**
     * 构建科室信息VO
     */
    private DepartmentDashboardVO.DepartmentInfoVO buildDepartmentInfoVO(Long departmentId, String departmentName) {
        return DepartmentDashboardVO.DepartmentInfoVO.builder()
                .departmentId(departmentId)
                .departmentName(departmentName)
                .build();
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