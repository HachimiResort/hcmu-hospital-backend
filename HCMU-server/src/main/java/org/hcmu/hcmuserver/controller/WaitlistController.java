package org.hcmu.hcmuserver.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.AppointmentDTO;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.WaitlistDTO;
import org.hcmu.hcmuserver.service.WaitlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "等待队列接口", description = "等待队列相关接口")
@RestController
@RequestMapping("waitlists")
@Validated
public class WaitlistController {

    @Autowired
    WaitlistService waitlistService;

    @AutoLog("创建等待队列")
    @Operation(description = "创建等待队列", summary = "创建等待队列")
    @PostMapping("")
    @PreAuthorize("@ex.hasSysAuthority('ADD_WAITLIST')")
    public Result<WaitlistDTO.WaitlistDetailDTO> createWaitlist(@RequestBody @Valid WaitlistDTO.WaitlistCreateDTO createDTO) {
        return waitlistService.createWaitlist(createDTO);
    }


    @AutoLog("获取等待队列列表")
    @Operation(description = "获取等待队列列表", summary = "获取等待队列列表")
    @GetMapping("")
    @PreAuthorize("@ex.hasSysAuthority('CHECK_WAITLIST')")
    public Result<PageDTO<WaitlistDTO.WaitlistListDTO>> getWaitlists(@ModelAttribute WaitlistDTO.WaitlistGetRequestDTO requestDTO) {
        return waitlistService.getWaitlists(requestDTO);
    }

    @AutoLog("获取等待队列详情")
    @Operation(description = "获取等待队列详情", summary = "获取等待队列详情")
    @GetMapping("/{waitlistId}")
    @PreAuthorize("@ex.hasSysAuthority('CHECK_WAITLIST')")
    public Result<WaitlistDTO.WaitlistDetailDTO> getWaitlistById(@PathVariable Long waitlistId) {
        return waitlistService.getWaitlistById(waitlistId);
    }

    @AutoLog("更新等待队列信息")
    @Operation(description = "更新等待队列信息", summary = "更新等待队列信息")
    @PutMapping("/{waitlistId}")
    @PreAuthorize("@ex.hasSysAuthority('ALT_WAITLIST')")
    public Result<String> updateWaitlist(@PathVariable Long waitlistId,
                                         @RequestBody @Valid WaitlistDTO.WaitlistUpdateDTO updateDTO) {
        return waitlistService.updateWaitlistById(waitlistId, updateDTO);
    }

    @AutoLog("删除等待队列（逻辑删除）")
    @Operation(description = "删除等待队列（逻辑删除）", summary = "删除等待队列")
    @DeleteMapping("/{waitlistId}")
    @PreAuthorize("@ex.hasSysAuthority('DEL_WAITLIST')")
    public Result<String> deleteWaitlist(@PathVariable Long waitlistId) {
        return waitlistService.deleteWaitlistById(waitlistId);
    }

    @AutoLog("患者加入候补队列")
    @Operation(description = "患者加入候补队列", summary = "患者加入候补队列")
    @PostMapping("/join")
    public Result<WaitlistDTO.WaitlistDetailDTO> patientJoinWaitlist(@RequestBody @Valid WaitlistDTO.PatientJoinDTO joinDTO) {
        return waitlistService.patientJoinWaitlist(joinDTO);
    }

    @AutoLog("候补支付")
    @Operation(description = "候补支付", summary = "候补支付")
    @PostMapping("/{waitlistId}/pay")
    public Result<AppointmentDTO.AppointmentListDTO> payWaitlist(@PathVariable Long waitlistId) {
        return waitlistService.payWaitlist(waitlistId);
    }
}
