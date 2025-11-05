package org.hcmu.hcmuserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.AppointmentDTO;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmuserver.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "挂号预约接口", description = "预约相关接口")
@RestController
@RequestMapping("Appointment")
@Validated
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @AutoLog("查询预约列表")
    @Operation(description = "分页查询预约列表", summary = "查询预约列表（CHECK_APPOINTMENT）")
    @PreAuthorize("@ex.hasSysAuthority('CHECK_APPOINTMENT')")
    @GetMapping("")
    public Result<PageDTO<AppointmentDTO.AppointmentListDTO>>getAppointments(@ModelAttribute AppointmentDTO.AppointmentGetRequsetDTO requestDTO) {
        return appointmentService.getAppointments(requestDTO);
    }
    @AutoLog("根据预约Id预约详情")
    @Operation(description = "获取患者预约详情", summary = "获取预约详情（CHECK_APPOINTMENT）")
    @PreAuthorize("@ex.hasSysAuthority('CHECK_APPOINTMENT')")
    @GetMapping("/{appointmentId}")
    public Result<AppointmentDTO.AppointmentDetailDTO> getAppointmentById(@PathVariable Long appointmentId) {
        return appointmentService.getAppointmentById(appointmentId);
    }



}
