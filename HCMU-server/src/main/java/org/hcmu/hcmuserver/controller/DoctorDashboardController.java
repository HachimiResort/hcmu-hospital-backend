package org.hcmu.hcmuserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.DoctorDashboardDTO;
import org.hcmu.hcmupojo.vo.DoctorDashboardVO;
import org.hcmu.hcmuserver.service.DoctorDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 医生大屏接口
 */
@Tag(name = "医生大屏接口", description = "医生大屏数据相关接口")
@RestController
@RequestMapping("dashboard/doctors")
@Validated
public class DoctorDashboardController {

    @Autowired
    private DoctorDashboardService doctorDashboardService;

    @AutoLog("获取医生就诊量排行")
    @Operation(description = "获取医生就诊量排行", summary = "获取医生就诊量排行")
    @PostMapping("/visit-rank")
    public Result<DoctorDashboardVO.DoctorVisitRankVO> getDoctorVisitRank(
            @RequestBody @Valid DoctorDashboardDTO.DoctorVisitRankDTO requestDTO) {
        return doctorDashboardService.getDoctorVisitRank(requestDTO);
    }

    @AutoLog("获取医生收入排行")
    @Operation(description = "获取医生收入排行", summary = "获取医生收入排行")
    @PostMapping("/income-rank")
    public Result<DoctorDashboardVO.DoctorIncomeRankVO> getDoctorIncomeRank(
            @RequestBody @Valid DoctorDashboardDTO.DoctorIncomeRankDTO requestDTO) {
        return doctorDashboardService.getDoctorIncomeRank(requestDTO);
    }

    @AutoLog("获取医生预约率统计")
    @Operation(description = "获取医生预约率统计", summary = "获取医生预约率统计")
    @PostMapping("/appointment-rate")
    public Result<DoctorDashboardVO.DoctorAppointmentRateVO> getDoctorAppointmentRate(
            @RequestBody @Valid DoctorDashboardDTO.DoctorAppointmentRateDTO requestDTO) {
        return doctorDashboardService.getDoctorAppointmentRate(requestDTO);
    }
}