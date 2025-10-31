package org.hcmu.hcmuserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.DepartmentDTO;
import org.hcmu.hcmupojo.dto.DoctorProfileDTO;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmuserver.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "科室接口", description = "科室信息相关接口")
@RestController
@RequestMapping("departments")
@Validated
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @AutoLog("创建科室")
    @Operation(description = "创建科室", summary = "创建科室('DEPT_MANAGE')")
    @PostMapping("")
    @PreAuthorize("@ex.hasSysAuthority('ADD_DEPART')")
    public Result<DepartmentDTO.DepartmentListDTO> createDepartment(@RequestBody @Valid DepartmentDTO.DepartmentCreateDTO createDTO) {
        return departmentService.createDepartment(createDTO);
    }

    @AutoLog("获取所有科室")
    @Operation(description = "获取所有科室", summary = "获取所有科室")
    @GetMapping("")
    public Result<PageDTO<DepartmentDTO.DepartmentListDTO>> getAllDepartments(@ModelAttribute DepartmentDTO.DepartmentGetRequestDTO requestDTO) {
        return departmentService.findAllDepartments(requestDTO);
    }

    @AutoLog("获取科室详情")
    @Operation(description = "获取科室详情", summary = "获取科室详情")
    @GetMapping("/{departmentId}")
    public Result<DepartmentDTO.DepartmentListDTO> getDepartmentById(@PathVariable Long departmentId) {
        return departmentService.findDepartmentById(departmentId);
    }

    @AutoLog("更新科室信息")
    @Operation(description = "更新科室信息", summary = "更新科室信息('DEPT_MANAGE')")
    @PutMapping("/{departmentId}")
    @PreAuthorize("@ex.hasSysAuthority('ALT_DEPART')")
    public Result<String> updateDepartment(@PathVariable Long departmentId, @RequestBody @Valid DepartmentDTO.DepartmentUpdateDTO updateDTO) {
        return departmentService.updateDepartmentById(departmentId, updateDTO);
    }

    @AutoLog("删除科室（逻辑删除）")
    @Operation(description = "删除科室（逻辑删除）", summary = "删除科室('DEPT_MANAGE')")
    @DeleteMapping("/{departmentId}")
    @PreAuthorize("@ex.hasSysAuthority('DEL_DEPART')")
    public Result<String> deleteDepartment(@PathVariable Long departmentId) {
        return departmentService.deleteDepartmentById(departmentId);
    }

    // 新增：批量删除科室
    @AutoLog("批量删除科室（逻辑删除）")
    @Operation(description = "批量删除科室", summary = "批量删除科室('DEPT_MANAGE')")
    @DeleteMapping("/batch")
    @PreAuthorize("@ex.hasSysAuthority('DEL_DEPART')")
    public Result<String> batchDeleteDepartments(@RequestBody List<Long> departmentIds) {
        return departmentService.batchDeleteDepartments(departmentIds);
    }

    // DepartmentController.java 中新增
    @AutoLog("查询科室下所有医生")
    @Operation(description = "查询指定科室的所有医生", summary = "查询科室下医生列表")
    @GetMapping("/{departmentId}/doctor")
    public Result<PageDTO<DoctorProfileDTO.DoctorProfileListDTO>> getDoctorsByDepartment(
            @PathVariable Long departmentId,
            @ModelAttribute DoctorProfileDTO.DoctorProfileGetRequestDTO requestDTO) {
        // 直接调用DepartmentService的方法，无需注入DoctorProfileService
        return departmentService.getDoctorsByDepartment(departmentId, requestDTO);
    }
}