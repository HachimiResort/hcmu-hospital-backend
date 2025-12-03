package org.hcmu.hcmuserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.DepartmentDashboardDTO;
import org.hcmu.hcmupojo.vo.DepartmentDashboardVO;
import org.hcmu.hcmuserver.service.DepartmentDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 科室可视化大屏接口
 */
@Tag(name = "科室大屏接口", description = "科室可视化大屏数据相关接口")
@RestController
@RequestMapping("dashboard/departments")
@Validated
public class DepartmentDashboardController {

    @Autowired
    private DepartmentDashboardService departmentDashboardService;

    @AutoLog("获取科室负荷统计")
    @Operation(description = "获取科室负荷统计", summary = "获取科室负荷统计")
    @GetMapping("/department-load")
    public Result<DepartmentDashboardVO.LoadStatisticsVO> getDepartmentLoadStatistics() {
        return departmentDashboardService.getDepartmentLoadStatistics();
    }

    @AutoLog("获取科室预约排行")
    @Operation(description = "获取科室预约排行", summary = "获取科室预约排行")
    @PostMapping("/appointment-rank")
    public Result<DepartmentDashboardVO.AppointmentRankVO> getDepartmentAppointmentRank(
            @RequestBody @Valid DepartmentDashboardDTO.AppointmentRankDTO requestDTO) {
        return departmentDashboardService.getDepartmentAppointmentRank(requestDTO);
    }
}
