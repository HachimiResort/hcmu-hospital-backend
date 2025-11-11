package org.hcmu.hcmuserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.AppointmentDTO;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmuserver.service.AppointmentService;
import org.hcmu.hcmuserver.service.UserService;
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
    private UserService userService;

    @Autowired
    private AppointmentService appointmentService;

    @AutoLog("查询预约列表")
    @GetMapping("")
    @PreAuthorize("@ex.hasSysAuthority('CHECK_APPOINTMENT')")
    public Result<PageDTO<AppointmentDTO.AppointmentListDTO>>getAppointments(@ModelAttribute AppointmentDTO.AppointmentGetRequestDTO requestDTO) {
        return appointmentService.getAppointments(requestDTO);
    }
    @AutoLog("根据预约Id预约详情")
    @GetMapping("/{appointmentId}")
    @PreAuthorize("@ex.hasSysAuthority('CHECK_APPOINTMENT')")
    public Result<AppointmentDTO.AppointmentListDTO> getAppointmentById(@PathVariable Long appointmentId) {
        return appointmentService.getAppointmentById(appointmentId);
    }

    @AutoLog("取消预约")
    @Operation(description = "取消预约", summary = "取消预约")
    @PutMapping("/{appointmentId}/cancel")
    public Result<AppointmentDTO.AppointmentListDTO> cancelAppointment(
            @PathVariable Long appointmentId,
            @RequestBody @Valid AppointmentDTO.AppointmentCancelDTO cancelDTO) {
        return appointmentService.cancelAppointment(appointmentId, cancelDTO.getReason());
    }

}
