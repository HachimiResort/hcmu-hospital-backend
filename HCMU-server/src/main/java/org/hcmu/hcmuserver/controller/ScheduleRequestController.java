package org.hcmu.hcmuserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.ScheduleRequestDTO;
import org.hcmu.hcmuserver.service.ScheduleRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "排班申请接口", description = "调班、休假、加号申请相关接口")
@RestController
@RequestMapping("schedule-requests")
@Validated
public class ScheduleRequestController {

    @Autowired
    private ScheduleRequestService scheduleRequestService;

    @AutoLog("创建排班申请")
    @Operation(description = "创建排班申请", summary = "创建排班申请('ADD_SCHEDULE_REQUEST')")
    @PostMapping("")
    public Result<ScheduleRequestDTO.ScheduleRequestDetailDTO> createScheduleRequest(@RequestBody @Valid ScheduleRequestDTO.ScheduleRequestCreateDTO createDTO) {
        return scheduleRequestService.createScheduleRequest(createDTO);
    }

    @AutoLog("获取排班申请列表")
    @Operation(description = "获取排班申请列表", summary = "获取排班申请列表('CHECK_SCHEDULE_REQUEST')")
    @GetMapping("")
    public Result<PageDTO<ScheduleRequestDTO.ScheduleRequestListDTO>> getScheduleRequests(@ModelAttribute ScheduleRequestDTO.ScheduleRequestGetRequestDTO requestDTO) {
        return scheduleRequestService.getScheduleRequests(requestDTO);
    }

    @AutoLog("获取排班申请详情")
    @Operation(description = "获取排班申请详情", summary = "获取排班申请详情('CHECK_SCHEDULE_REQUEST')")
    @GetMapping("/{requestId}")
    public Result<ScheduleRequestDTO.ScheduleRequestDetailDTO> getScheduleRequestById(@PathVariable Long requestId) {
        return scheduleRequestService.getScheduleRequestById(requestId);
    }

    @AutoLog("更新排班申请")
    @Operation(description = "更新排班申请", summary = "更新排班申请('ALT_SCHEDULE_REQUEST')")
    @PutMapping("/{requestId}")
    public Result<String> updateScheduleRequest(@PathVariable Long requestId,
                                                @RequestBody @Valid ScheduleRequestDTO.ScheduleRequestUpdateDTO updateDTO) {
        return scheduleRequestService.updateScheduleRequestById(requestId, updateDTO);
    }

    @AutoLog("删除排班申请")
    @Operation(description = "删除排班申请", summary = "删除排班申请，（管理员用）('DEL_SCHEDULE_REQUEST')")
    @DeleteMapping("/{requestId}")
    @PreAuthorize("@ex.hasSysAuthority('DEL_SCHEDULE_REQUEST')")
    public Result<String> deleteScheduleRequest(@PathVariable Long requestId) {
        return scheduleRequestService.deleteScheduleRequestById(requestId);
    }

    @AutoLog("审批排班申请")
    @Operation(description = "审批排班申请（同意/拒绝）", summary = "审批排班申请('APPROVE_SCHEDULE_REQUEST')")
    @PostMapping("/{requestId}/handle")
    @PreAuthorize("@ex.hasSysAuthority('APPROVE_SCHEDULE_REQUEST')")
    public Result<String> handleScheduleRequest(@PathVariable Long requestId,
                                                @RequestBody @Valid ScheduleRequestDTO.ScheduleRequestHandleDTO handleDTO) {
        return scheduleRequestService.handleScheduleRequest(requestId, handleDTO);
    }

    @AutoLog("撤销排班申请")
    @Operation(description = "撤销排班申请", summary = "撤销排班申请")
    @PostMapping("/{requestId}/cancel")
    public Result<String> cancelScheduleRequest(@PathVariable Long requestId) {
        return scheduleRequestService.cancelScheduleRequest(requestId);
    }
}
