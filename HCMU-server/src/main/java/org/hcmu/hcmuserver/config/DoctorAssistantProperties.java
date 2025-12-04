package org.hcmu.hcmuserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 配置智能医生助手调用的上游 OpenAI 兼容接口。
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.doctor-assistant")
public class DoctorAssistantProperties {

    /**
     * 上游接口基础地址，例如 http://127.0.0.1:7861
     */
    private String baseUrl = "http://127.0.0.1:7861";

    /**
     * chat completions 路径
     */
    private String chatPath = "/v1/chat/completions";

    /**
     * 上游接口访问密钥。
     */
    private String apiKey = "yuyu123";

    /**
     * 默认模型，可被请求覆盖。
     */
    private String model = "gpt-4o-mini";

    /**
     * 默认系统提示词，留空则不自动注入。
     */
    private String systemPrompt;

    /**
     * 连接和读取超时时间（毫秒）
     */
    private int connectTimeoutMs = 10_000;
    private int readTimeoutMs = 120_000;
}
