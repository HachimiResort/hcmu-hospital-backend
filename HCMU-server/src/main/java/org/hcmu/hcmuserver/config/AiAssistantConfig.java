package org.hcmu.hcmuserver.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class AiAssistantConfig {

    private final DoctorAssistantProperties doctorAssistantProperties;

    @Bean(name = "aiAssistantRestTemplate")
    public RestTemplate aiAssistantRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(doctorAssistantProperties.getConnectTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(doctorAssistantProperties.getReadTimeoutMs()))
                .build();
    }
}
