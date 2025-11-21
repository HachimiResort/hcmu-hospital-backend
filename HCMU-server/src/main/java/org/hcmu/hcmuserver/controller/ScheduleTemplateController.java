package org.hcmu.hcmuserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.ScheduleTemplateDTO;
import org.hcmu.hcmuserver.service.ScheduleTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "排班模板接口", description = "排班模板及模板内排班相关接口")
@RestController
@RequestMapping("schedule-templates")
@Validated
public class ScheduleTemplateController {

    @Autowired
    private ScheduleTemplateService scheduleTemplateService;

    @AutoLog("创建排班模板")
    @Operation(description = "创建排班模板", summary = "创建排班模板('ADD_TEMPLATE')")
    @PostMapping("")
    @PreAuthorize("@ex.hasSysAuthority('ADD_TEMPLATE')")
    public Result<ScheduleTemplateDTO.TemplateListDTO> createTemplate(
            @RequestBody @Valid ScheduleTemplateDTO.TemplateCreateDTO createDTO) {
        return scheduleTemplateService.createTemplate(createDTO);
    }

    @AutoLog("获取排班模板列表")
    @Operation(description = "获取排班模板列表", summary = "获取排班模板列表('CHECK_TEMPLATE')")
    @GetMapping("")
    @PreAuthorize("@ex.hasSysAuthority('CHECK_TEMPLATE')")
    public Result<PageDTO<ScheduleTemplateDTO.TemplateListDTO>> getAllTemplates(
            @ModelAttribute ScheduleTemplateDTO.TemplateGetRequestDTO requestDTO) {
        return scheduleTemplateService.findAllTemplates(requestDTO);
    }

    @AutoLog("获取排班模板详情")
    @Operation(description = "获取排班模板详情", summary = "获取排班模板详情('CHECK_TEMPLATE')")
    @GetMapping("/{templateId}")
    @PreAuthorize("@ex.hasSysAuthority('CHECK_TEMPLATE')")
    public Result<ScheduleTemplateDTO.TemplateListDTO> getTemplateById(@PathVariable Long templateId) {
        return scheduleTemplateService.findTemplateById(templateId);
    }

    @AutoLog("更新排班模板信息")
    @Operation(description = "更新排班模板信息", summary = "更新排班模板信息('ALT_TEMPLATE')")
    @PutMapping("/{templateId}")
    @PreAuthorize("@ex.hasSysAuthority('ALT_TEMPLATE')")
    public Result<String> updateTemplate(@PathVariable Long templateId,
                                         @RequestBody @Valid ScheduleTemplateDTO.TemplateUpdateDTO updateDTO) {
        return scheduleTemplateService.updateTemplateById(templateId, updateDTO);
    }

    @AutoLog("删除排班模板（逻辑删除）")
    @Operation(description = "删除排班模板（逻辑删除）", summary = "删除排班模板('DEL_TEMPLATE')")
    @DeleteMapping("/{templateId}")
    @PreAuthorize("@ex.hasSysAuthority('DEL_TEMPLATE')")
    public Result<String> deleteTemplate(@PathVariable Long templateId) {
        return scheduleTemplateService.deleteTemplateById(templateId);
    }

    @AutoLog("在模板下创建排班")
    @Operation(description = "在模板下创建排班", summary = "在模板下创建排班('ADD_TEMPLATE')")
    @PostMapping("/{templateId}/schedules")
    @PreAuthorize("@ex.hasSysAuthority('ADD_TEMPLATE')")
    public Result<ScheduleTemplateDTO.TemplateScheduleListDTO> createSchedule(
            @PathVariable Long templateId,
            @RequestBody @Valid ScheduleTemplateDTO.TemplateScheduleCreateDTO createDTO) {
        return scheduleTemplateService.createSchedule(templateId, createDTO);
    }

    @AutoLog("获取模板下的排班列表")
    @Operation(description = "获取模板下的排班列表", summary = "获取模板下的排班列表('CHECK_TEMPLATE')")
    @GetMapping("/{templateId}/schedules")
    @PreAuthorize("@ex.hasSysAuthority('CHECK_TEMPLATE')")
    public Result<PageDTO<ScheduleTemplateDTO.TemplateScheduleListDTO>> getSchedules(
            @PathVariable Long templateId,
            @ModelAttribute ScheduleTemplateDTO.TemplateScheduleGetRequestDTO requestDTO) {
        return scheduleTemplateService.findSchedules(templateId, requestDTO);
    }

    @AutoLog("获取模板排班详情")
    @Operation(description = "获取模板排班详情", summary = "获取模板排班详情('CHECK_TEMPLATE')")
    @GetMapping("/{templateId}/schedules/{scheduleId}")
    @PreAuthorize("@ex.hasSysAuthority('CHECK_TEMPLATE')")
    public Result<ScheduleTemplateDTO.TemplateScheduleListDTO> getScheduleById(@PathVariable Long templateId,
                                                                               @PathVariable Long scheduleId) {
        return scheduleTemplateService.findScheduleById(templateId, scheduleId);
    }

    @AutoLog("更新模板排班信息")
    @Operation(description = "更新模板排班信息", summary = "更新模板排班信息('ALT_TEMPLATE')")
    @PutMapping("/{templateId}/schedules/{scheduleId}")
    @PreAuthorize("@ex.hasSysAuthority('ALT_TEMPLATE')")
    public Result<String> updateSchedule(@PathVariable Long templateId,
                                         @PathVariable Long scheduleId,
                                         @RequestBody @Valid ScheduleTemplateDTO.TemplateScheduleUpdateDTO updateDTO) {
        return scheduleTemplateService.updateScheduleById(templateId, scheduleId, updateDTO);
    }

    @AutoLog("删除模板排班（逻辑删除）")
    @Operation(description = "删除模板排班（逻辑删除）", summary = "删除模板排班('DEL_TEMPLATE')")
    @DeleteMapping("/{templateId}/schedules/{scheduleId}")
    @PreAuthorize("@ex.hasSysAuthority('DEL_TEMPLATE')")
    public Result<String> deleteSchedule(@PathVariable Long templateId, @PathVariable Long scheduleId) {
        return scheduleTemplateService.deleteScheduleById(templateId, scheduleId);
    }
}
