package org.hcmu.hcmuserver.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.WaitlistDTO;
import org.hcmu.hcmuserver.service.WaitlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "等待队列接口", description = "等待队列相关接口")
@RestController
@RequestMapping("waitlists")
@Validated
public class WaitlistController {

    @Autowired
    WaitlistService waitlistService;

    @AutoLog("获取等待队列列表")
    @GetMapping("")
    public Result<PageDTO<WaitlistDTO.WaitlistListDTO>> getWaitlists(@ModelAttribute  WaitlistDTO.WaitlistGetRequestDTO requestDTO) {
        return waitlistService.getWaitlists(requestDTO);
    }

    @AutoLog("获取等待队列列表")
    @GetMapping("/{waitlistId}")
    public Result<WaitlistDTO.WaitlistDetailDTO> getWaitlistById(@PathVariable Long waitlistId){
        return waitlistService.getWaitlistById(waitlistId);
    }


}
