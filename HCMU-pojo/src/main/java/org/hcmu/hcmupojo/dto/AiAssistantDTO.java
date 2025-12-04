package org.hcmu.hcmupojo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 智能医生助手 Chat Completion 相关 DTO（兼容 OpenAI 接口格式）
 */
public class AiAssistantDTO {

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChatCompletionRequest {
        private String model;

        @NotEmpty(message = "messages不能为空")
        private List<ChatMessage> messages;

        private Double temperature;

        @JsonProperty("top_p")
        private Double topP;

        @JsonProperty("max_tokens")
        private Integer maxTokens;

        private Boolean stream;

        /**
         * 额外提供的系统提示词，如果填写会自动注入到消息列表开头。
         */
        @JsonProperty("system_prompt")
        private String systemPrompt;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChatMessage {
        private String role;
        private Object content;
        private String name;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChatCompletionResponse {
        private String id;
        private String object;
        private Long created;
        private String model;

        @JsonProperty("system_fingerprint")
        private String systemFingerprint;

        private List<ChatChoice> choices;

        private Usage usage;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChatChoice {
        private Integer index;
        private ChatMessage message;

        @JsonProperty("finish_reason")
        private String finishReason;

        /**
         * 预留字段，兼容 logprobs 等扩展数据。
         */
        private Map<String, Object> logprobs;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}
