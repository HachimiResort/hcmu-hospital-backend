package org.hcmu.hcmuserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.DoctorProfileDTO;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmuserver.service.DoctorProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "医生档案接口", description = "医生档案信息相关接口")
@RestController
@RequestMapping("doctor-profiles")
@Validated
public class DoctorProfileController {

    @Autowired
    private DoctorProfileService doctorProfileService;

    @AutoLog("创建医生档案（已废弃）")
    @Operation(description = "创建医生档案", summary = "创建医生档案('ADD_DOCTOR')")
    @PostMapping("")
    @PreAuthorize("@ex.hasSysAuthority('ADD_DOCTOR')")
    public Result<DoctorProfileDTO.DoctorProfileListDTO> createDoctorProfile(@RequestBody @Valid DoctorProfileDTO.DoctorProfileCreateDTO createDTO) {
        return doctorProfileService.createDoctorProfile(createDTO);
    }

    @AutoLog("查询医生档案列表")
    @Operation(description = "分页查询医生档案列表", summary = "查询医生档案列表('CHECK_DOCTOR')")
    @PreAuthorize("@ex.hasSysAuthority('CHECK_DOCTOR')")
    @GetMapping("")
    public Result<PageDTO<DoctorProfileDTO.DoctorProfileListDTO>> getDoctorProfiles(@ModelAttribute DoctorProfileDTO.DoctorProfileGetRequestDTO requestDTO) {
        return doctorProfileService.getDoctorProfiles(requestDTO);
    }

    @AutoLog("通过用户Id获取医生档案")
    @Operation(description = "获取医生档案详情", summary = "获取医生档案详情(‘CHECK_DOCTOR’)")
    @PreAuthorize("@ex.hasSysAuthority('CHECK_DOCTOR')")
    @GetMapping("/{userId}")
    public Result<DoctorProfileDTO.DoctorProfileDetailDTO> getDoctorProfileByUserId(@PathVariable Long userId) {
        return doctorProfileService.getDoctorProfileByUserId(userId);
    }

    /**
     * 查询所有医生信息
     * @return
     */
    @AutoLog("获取所有医生档案")
    @Operation(description = "获取所有医生的详细档案信息（已废弃）", summary = "获取所有医生档案")
    @GetMapping("/getAllDoctor")
    public Result<List<DoctorProfileDTO.DoctorProfileDetailDTO>> getAllDoctor() {
        return doctorProfileService.getAllDoctors();
    }

    @AutoLog("更新医生档案")
    @Operation(description = "更新医生档案信息", summary = "更新医生档案('ALT_DOCTOR')")
    @PutMapping("/{doctorProfileId}")
    @PreAuthorize("@ex.hasSysAuthority('ALT_DOCTOR')")
    public Result<String> updateDoctorProfile(@PathVariable Long doctorProfileId, @RequestBody @Valid DoctorProfileDTO.DoctorProfileUpdateDTO updateDTO) {
        return doctorProfileService.updateDoctorProfile(doctorProfileId, updateDTO);
    }

    @AutoLog("删除医生档案（逻辑删除）")
    @Operation(description = "删除医生档案", summary = "删除医生档案('DEL_DOCTOR')")
    @DeleteMapping("/{doctorProfileId}")
    @PreAuthorize("@ex.hasSysAuthority('DEL_DOCTOR')")
    public Result<String> deleteDoctorProfile(@PathVariable Long doctorProfileId) {
        return doctorProfileService.deleteDoctorProfile(doctorProfileId);
    }

    @AutoLog("批量删除医生档案（逻辑删除）")
    @Operation(description = "批量删除医生档案", summary = "批量删除医生档案('DEL_DOCTOR')")
    @DeleteMapping("/batch")
    @PreAuthorize("@ex.hasSysAuthority('DEL_DOCTOR')")
    public Result<String> batchDeleteDoctorProfiles(@RequestBody List<Long> doctorProfileIds) {
        return doctorProfileService.batchDeleteDoctorProfiles(doctorProfileIds);
    }
}