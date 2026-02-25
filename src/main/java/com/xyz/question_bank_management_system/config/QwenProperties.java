package com.xyz.question_bank_management_system.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
//读LLM配置
@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "app.qwen")
public class QwenProperties {
    private String baseUrl;
    private String apiKey;
    private String apiKeyFile;
    private String model;
    private Double temperature = 0.2;

    public String resolveApiKey() {
        String key = normalizeKey(apiKey);
        if (key != null && !key.isBlank()) {
            return key;
        }
        if (apiKeyFile == null || apiKeyFile.isBlank()) {
            return "";
        }
        try {
            Path path = Path.of(apiKeyFile);
            if (!Files.exists(path)) {
                log.warn("Qwen api key file does not exist: {}", apiKeyFile);
                return "";
            }
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String normalized = normalizeKey(line);
                if (normalized != null && !normalized.isBlank()) {
                    return normalized;
                }
            }
            return "";
        } catch (Exception ex) {
            log.warn("Failed to read qwen api key from file: {}", apiKeyFile, ex);
            return "";
        }
    }

    private String normalizeKey(String raw) {
        if (raw == null) {
            return "";
        }
        String value = raw.trim().replace("\uFEFF", "");
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            value = value.substring(1, value.length() - 1).trim();
        }
        return value;
    }
}
