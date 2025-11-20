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
@RequestMapping("appointments")
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
            @RequestBody AppointmentDTO.AppointmentCancelDTO cancelDTO) {
        return appointmentService.cancelAppointment(appointmentId, cancelDTO.getReason());
    }

    @AutoLog("支付预约")
    @Operation(description = "支付预约", summary = "支付预约")
    @PutMapping("/{appointmentId}/pay")
    public Result<AppointmentDTO.AppointmentListDTO> payAppointment(
            @PathVariable Long appointmentId) {
        return appointmentService.payAppointment(appointmentId);
    }

    @AutoLog("传呼预约")
    @Operation(description = "传呼预约", summary = "传呼预约")
    @PutMapping("/{appointmentId}/call")
    public Result<AppointmentDTO.AppointmentListDTO> callAppointment(
            @PathVariable Long appointmentId) {
        return appointmentService.callAppointment(appointmentId);
    }

    @AutoLog("完成就诊")
    @Operation(description = "完成就诊", summary = "完成就诊")
    @PutMapping("/{appointmentId}/complete")
    public Result<AppointmentDTO.AppointmentListDTO> completeAppointment(
            @PathVariable Long appointmentId) {
        return appointmentService.completeAppointment(appointmentId);
    }

    @AutoLog("未到诊")
    @Operation(description = "未到诊", summary = "未到诊")
    @PutMapping("/{appointmentId}/no-show")
    public Result<AppointmentDTO.AppointmentListDTO> noShowAppointment(
            @PathVariable Long appointmentId) {
        return appointmentService.noShowAppointment(appointmentId);
    }

}
