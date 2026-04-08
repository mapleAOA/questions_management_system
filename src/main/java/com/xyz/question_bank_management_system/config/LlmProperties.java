package com.xyz.question_bank_management_system.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "app.llm")
public class LlmProperties {

    private static final String DEFAULT_SYSTEM_PROMPT = "\u4f60\u662f\u9898\u5e93\u7ba1\u7406\u7cfb\u7edf\u7684\u5927\u6a21\u578b\u52a9\u624b\uff0c\u8bf7\u4e25\u683c\u6309\u8981\u6c42\u8f93\u51fa\u5185\u5bb9\uff1b\u5982\u679c\u8981\u6c42\u8f93\u51fa JSON\uff0c\u8bf7\u4e0d\u8981\u6dfb\u52a0\u4efb\u4f55\u591a\u4f59\u8bf4\u660e\u3002";
    private static final String MSG_KEY_FILE_NOT_FOUND = "\u5927\u6a21\u578b\u5bc6\u94a5\u6587\u4ef6\u4e0d\u5b58\u5728: {}";
    private static final String MSG_KEY_FILE_READ_FAILED = "\u8bfb\u53d6\u5927\u6a21\u578b\u5bc6\u94a5\u6587\u4ef6\u5931\u8d25: {}";

    private String systemPrompt = DEFAULT_SYSTEM_PROMPT;
    private List<ModelProvider> providers = new ArrayList<>();
    private ModelProvider qwen = legacyProvider("qwen", "Qwen 3.5");
    private ModelProvider deepseek = legacyProvider("deepseek", "DeepSeek R1");
    private ModelProvider ernie = legacyProvider("ernie", "ERNIE 4.5 Turbo 128K");

    public ModelProvider defaultProvider() {
        List<ModelProvider> availableProviders = questionAnalysisProviders();
        return availableProviders.isEmpty() ? null : availableProviders.get(0);
    }

    public List<ModelProvider> questionAnalysisProviders() {
        return normalizeEnabledProviders(resolveConfiguredProviders());
    }

    public ModelProvider getProvider(String providerKey) {
        String normalized = normalize(providerKey);
        if (normalized.isBlank()) {
            return defaultProvider();
        }
        for (ModelProvider provider : questionAnalysisProviders()) {
            if (normalized.equals(provider.resolveKey())
                    || normalized.equals(normalize(provider.resolveLabel()))
                    || normalized.equals(normalize(provider.getModel()))) {
                return provider;
            }
        }
        return null;
    }

    private List<ModelProvider> resolveConfiguredProviders() {
        if (providers != null && !providers.isEmpty()) {
            return providers;
        }
        List<ModelProvider> legacyProviders = new ArrayList<>();
        if (qwen != null) {
            legacyProviders.add(qwen);
        }
        if (deepseek != null) {
            legacyProviders.add(deepseek);
        }
        if (ernie != null) {
            legacyProviders.add(ernie);
        }
        return legacyProviders;
    }

    private List<ModelProvider> normalizeEnabledProviders(List<ModelProvider> configuredProviders) {
        List<ModelProvider> normalizedProviders = new ArrayList<>();
        Set<String> seenKeys = new LinkedHashSet<>();
        if (configuredProviders == null) {
            return normalizedProviders;
        }
        int index = 1;
        for (ModelProvider rawProvider : configuredProviders) {
            ModelProvider provider = normalizeProvider(rawProvider, "provider-" + index, null);
            index++;
            if (provider == null || !provider.isEnabled()) {
                continue;
            }
            if (!seenKeys.add(provider.resolveKey())) {
                continue;
            }
            normalizedProviders.add(provider);
        }
        return normalizedProviders;
    }

    private static ModelProvider legacyProvider(String key, String label) {
        ModelProvider provider = new ModelProvider();
        provider.setKey(key);
        provider.setLabel(label);
        return provider;
    }

    private ModelProvider normalizeProvider(ModelProvider provider, String key, String label) {
        if (provider == null) {
            return null;
        }
        if (provider.getKey() == null || provider.getKey().isBlank()) {
            if (provider.getModel() != null && !provider.getModel().isBlank()) {
                provider.setKey(provider.getModel());
            } else {
                provider.setKey(key);
            }
        }
        if (provider.getLabel() == null || provider.getLabel().isBlank()) {
            if (label != null && !label.isBlank()) {
                provider.setLabel(label);
            } else if (provider.getModel() != null && !provider.getModel().isBlank()) {
                provider.setLabel(provider.getModel());
            } else {
                provider.setLabel(provider.resolveKey());
            }
        }
        return provider;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    @Data
    public static class ModelProvider {
        private boolean enabled = true;
        private String key;
        private String label;
        private String baseUrl;
        private String apiKey;
        private String apiKeyFile;
        private String model;
        private Double temperature = 0.2;
        private boolean supportsTemperature = true;

        public String resolveApiKey() {
            String keyValue = normalizeKey(apiKey);
            if (!keyValue.isBlank()) {
                return keyValue;
            }
            if (apiKeyFile == null || apiKeyFile.isBlank()) {
                return "";
            }
            try {
                Path path = Path.of(apiKeyFile);
                if (!Files.exists(path)) {
                    log.warn(MSG_KEY_FILE_NOT_FOUND, apiKeyFile);
                    return "";
                }
                for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                    String normalized = normalizeKey(line);
                    if (!normalized.isBlank()) {
                        return normalized;
                    }
                }
                return "";
            } catch (Exception ex) {
                log.warn(MSG_KEY_FILE_READ_FAILED, apiKeyFile, ex);
                return "";
            }
        }

        public String resolveKey() {
            return key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
        }

        public String resolveLabel() {
            if (label != null && !label.isBlank()) {
                return label.trim();
            }
            if (model != null && !model.isBlank()) {
                return model.trim();
            }
            return resolveKey();
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
}
