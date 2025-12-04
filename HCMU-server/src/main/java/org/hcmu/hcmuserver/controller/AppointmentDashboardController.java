package org.hcmu.hcmuserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.AppointmentDashboardDTO;
import org.hcmu.hcmupojo.vo.AppointmentDashboardVO;
import org.hcmu.hcmuserver.service.AppointmentDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 号源可视化大屏接口
 */
@Tag(name = "号源大屏接口", description = "号源可视化大屏数据相关接口")
@RestController
@RequestMapping("dashboard/appointments")
@Validated
public class AppointmentDashboardController {

    @Autowired
    private AppointmentDashboardService appointmentDashboardService;

    @AutoLog("获取号源统计")
    @Operation(description = "获取号源统计", summary = "获取号源统计")
    @PostMapping("/statistics")
    public Result<AppointmentDashboardVO.AppointmentStatisticsVO> getAppointmentStatistics(
            @RequestBody @Valid AppointmentDashboardDTO.AppointmentStatisticsDTO requestDTO) {
        return appointmentDashboardService.getAppointmentStatistics(requestDTO);
    }

    @AutoLog("获取时段-预约量曲线图")
    @Operation(description = "获取时段-预约量曲线图", summary = "获取时段-预约量曲线图")
    @PostMapping("/trend")
    public Result<AppointmentDashboardVO.AppointmentTrendVO> getAppointmentTrend(
            @RequestBody @Valid AppointmentDashboardDTO.AppointmentTrendDTO requestDTO) {
        return appointmentDashboardService.getAppointmentTrend(requestDTO);
    }
}
