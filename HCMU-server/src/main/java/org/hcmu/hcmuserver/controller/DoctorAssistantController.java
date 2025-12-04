package org.hcmu.hcmuserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmupojo.dto.AiAssistantDTO;
import org.hcmu.hcmuserver.service.DoctorAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "智能医生助手", description = "基于 OpenAI Chat Completion 的医生助手接口")
@Slf4j
@RestController
@RequestMapping("/doctor-assistant")
@Validated
public class DoctorAssistantController {

    @Autowired
    private DoctorAssistantService doctorAssistantService;

    @AutoLog("智能医生助手对话")
    @Operation(summary = "智能医生助手对话", description = "遵循 OpenAI chat completions 格式，内部转发到本地推理服务")
    @PostMapping("/v1/chat/completions")
    public AiAssistantDTO.ChatCompletionResponse chat(
            @Valid @RequestBody AiAssistantDTO.ChatCompletionRequest request) {
        return doctorAssistantService.chat(request);
    }
}
