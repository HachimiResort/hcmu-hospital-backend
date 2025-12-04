package org.hcmu.hcmuserver.service;

import org.hcmu.hcmupojo.dto.AiAssistantDTO;

public interface DoctorAssistantService {

    /**
     * 调用智能医生助手（OpenAI 兼容接口）进行对话
     */
    AiAssistantDTO.ChatCompletionResponse chat(AiAssistantDTO.ChatCompletionRequest request);
}
