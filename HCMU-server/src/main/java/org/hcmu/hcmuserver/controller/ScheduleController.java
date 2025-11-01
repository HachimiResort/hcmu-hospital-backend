package org.hcmu.hcmuserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.ScheduleDTO;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmuserver.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "排班接口", description = "医生排班日程相关接口")
@RestController
@RequestMapping("schedules")
@Validated
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @AutoLog("创建排班")
    @Operation(description = "创建排班", summary = "创建排班('ADD_SCHEDULE')")
    @PostMapping("")
    @PreAuthorize("@ex.hasSysAuthority('ADD_SCHEDULE')")
    public Result<ScheduleDTO.ScheduleListDTO> createSchedule(@RequestBody @Valid ScheduleDTO.ScheduleCreateDTO createDTO) {
        return scheduleService.createSchedule(createDTO);
    }

    @AutoLog("获取所有排班")
    @Operation(description = "获取所有排班", summary = "获取所有排班")
    @GetMapping("")
    public Result<PageDTO<ScheduleDTO.ScheduleListDTO>> getAllSchedules(@ModelAttribute ScheduleDTO.ScheduleGetRequestDTO requestDTO) {
        return scheduleService.findAllSchedules(requestDTO);
    }

    @AutoLog("获取排班详情")
    @Operation(description = "获取排班详情", summary = "获取排班详情")
    @GetMapping("/{scheduleId}")
    public Result<ScheduleDTO.ScheduleListDTO> getScheduleById(@PathVariable Long scheduleId) {
        return scheduleService.findScheduleById(scheduleId);
    }

    @AutoLog("更新排班信息")
    @Operation(description = "更新排班信息", summary = "更新排班信息('ALT_SCHEDULE')")
    @PutMapping("/{scheduleId}")
    @PreAuthorize("@ex.hasSysAuthority('ALT_SCHEDULE')")
    public Result<String> updateSchedule(@PathVariable Long scheduleId, @RequestBody @Valid ScheduleDTO.ScheduleUpdateDTO updateDTO) {
        return scheduleService.updateScheduleById(scheduleId, updateDTO);
    }

    @AutoLog("删除排班（逻辑删除）")
    @Operation(description = "删除排班（逻辑删除）", summary = "删除排班('DEL_SCHEDULE')")
    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("@ex.hasSysAuthority('DEL_SCHEDULE')")
    public Result<String> deleteSchedule(@PathVariable Long scheduleId) {
        return scheduleService.deleteScheduleById(scheduleId);
    }

    // 新增：批量删除排班
    @AutoLog("批量删除排班（逻辑删除）")
    @Operation(description = "批量删除排班", summary = "批量删除排班('DEL_SCHEDULE')")
    @DeleteMapping("/batch")
    @PreAuthorize("@ex.hasSysAuthority('DEL_SCHEDULE')")
    public Result<String> batchDeleteSchedules(@RequestBody List<Long> scheduleIds) {
        return scheduleService.batchDeleteSchedules(scheduleIds);
    }



}
