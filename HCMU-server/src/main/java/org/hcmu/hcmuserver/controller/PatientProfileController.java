package org.hcmu.hcmuserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.PatientProfileDTO;
import org.hcmu.hcmupojo.dto.AppointmentDTO;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmuserver.service.AppointmentService;
import org.hcmu.hcmuserver.service.PatientProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "患者档案接口", description = "患者档案信息相关接口")
@RestController
@RequestMapping("patient-profiles")
@Validated
public class PatientProfileController {

    @Autowired
    private PatientProfileService patientProfileService;

    @Autowired
    private AppointmentService appointmentService;

    @Deprecated
    @AutoLog("创建患者档案（已废弃）")
    @Operation(description = "创建患者档案", summary = "创建患者档案('ADD_PATIENT')")
    @PostMapping("")
    @PreAuthorize("@ex.hasSysAuthority('ADD_PATIENT')")
    public Result<PatientProfileDTO.PatientProfileListDTO> createPatientProfile(@RequestBody @Valid PatientProfileDTO.PatientProfileCreateDTO createDTO) {
        return patientProfileService.createPatientProfile(createDTO);
    }

    @AutoLog("查询患者档案列表")
    @Operation(description = "分页查询患者档案列表", summary = "查询患者档案列表('CHECK_PATIENT')")
    @PreAuthorize("@ex.hasSysAuthority('CHECK_PATIENT')")
    @GetMapping("")
    public Result<PageDTO<PatientProfileDTO.PatientProfileListDTO>> getPatientProfiles(@ModelAttribute PatientProfileDTO.PatientProfileGetRequestDTO requestDTO) {
        return patientProfileService.getPatientProfiles(requestDTO);
    }

    @AutoLog("通过用户Id获取患者档案")
    @Operation(description = "获取患者档案详情", summary = "获取患者档案详情(‘CHECK_PATIENT’)")
    @PreAuthorize("@ex.hasSysAuthority('CHECK_PATIENT') || @ex.isSelf(#userId)")
    @GetMapping("/{userId}")
    public Result<PatientProfileDTO.PatientProfileDetailDTO> getPatientProfileByUserId(@PathVariable Long userId) {
        return patientProfileService.getPatientProfileByUserId(userId);
    }

    @AutoLog("更新患者档案")
    @Operation(description = "更新患者档案信息", summary = "更新患者档案('ALT_PATIENT')")
    @PutMapping("/{userId}")
    @PreAuthorize("@ex.hasSysAuthority('ALT_PATIENT')")
    public Result<String> updatePatientProfile(@PathVariable Long userId, @RequestBody @Valid PatientProfileDTO.PatientProfileUpdateDTO updateDTO) {
        return patientProfileService.updatePatientProfileByUserId(userId, updateDTO);
    }

    @AutoLog("患者更新自己的档案")
    @Operation(description = "患者更新自己的档案信息", summary = "患者更新自己的档案")
    @PutMapping("/self")
    public Result<String> updateSelfPatientProfile(@RequestBody @Valid PatientProfileDTO.PatientProfileUpdateSelfDTO updateDTO) {
        return patientProfileService.updateSelfPatientProfile(updateDTO);
    }

    @Deprecated
    @AutoLog("获取所有患者档案")
    @Operation(description = "获取所有患者的详细档案信息（已废弃）", summary = "获取所有患者档案")
    @GetMapping("/getAllPatient")
    @PreAuthorize("@ex.hasSysAuthority('CHECK_PATIENT')")
    public Result<List<PatientProfileDTO.PatientProfileDetailDTO>> getAllPatient() {
        return patientProfileService.getAllPatients();
    }

    @Deprecated
    @AutoLog("删除患者档案（逻辑删除）")
    @Operation(description = "删除患者档案", summary = "删除患者档案('DEL_PATIENT')")
    @DeleteMapping("/{patientProfileId}")
    @PreAuthorize("@ex.hasSysAuthority('DEL_PATIENT')")
    public Result<String> deletePatientProfile(@PathVariable Long patientProfileId) {
        return patientProfileService.deletePatientProfile(patientProfileId);
    }

    @Deprecated
    @AutoLog("批量删除患者档案（逻辑删除）")
    @Operation(description = "批量删除患者档案", summary = "批量删除患者档案('DEL_PATIENT')")
    @DeleteMapping("/batch")
    @PreAuthorize("@ex.hasSysAuthority('DEL_PATIENT')")
    public Result<String> batchDeletePatientProfiles(@RequestBody List<Long> patientProfileIds) {
        return patientProfileService.batchDeletePatientProfiles(patientProfileIds);
    }

    @AutoLog("根据用户id查找预约")
    @Operation(description = "根据用户id查找预约", summary = "根据用户id查找预约('CHECK_APPOINTMENT')")
    @GetMapping("/{userId}/appointments")
    @PreAuthorize("@ex.hasSysAuthority('CHECK_APPOINTMENT') || @ex.isSelf(#userId)")
    public Result<PageDTO<AppointmentDTO.AppointmentListDTO>> getAppointmentByPatientId(@PathVariable Long userId, @ModelAttribute AppointmentDTO.AppointmentGetRequestDTO appointmentGetRequestDTO) {
        appointmentGetRequestDTO.setPatientUserId(userId);
        return appointmentService.getAppointments(appointmentGetRequestDTO);
    }
}