package com.xyz.question_bank_management_system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.qwen")
public class QwenProperties {
    private String baseUrl;
    private String apiKey;
    private String model;
    private Double temperature = 0.2;
}
