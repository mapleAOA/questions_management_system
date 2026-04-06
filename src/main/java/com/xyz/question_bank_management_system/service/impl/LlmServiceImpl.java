package com.xyz.question_bank_management_system.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xyz.question_bank_management_system.config.QwenProperties;
import com.xyz.question_bank_management_system.entity.QbLlmCall;
import com.xyz.question_bank_management_system.mapper.QbLlmCallMapper;
import com.xyz.question_bank_management_system.service.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmServiceImpl implements LlmService {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

    private final QwenProperties qwenProperties;
    private final QbLlmCallMapper llmCallMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .build();

    @Override
    public QbLlmCall chatCompletion(int bizType, long bizId, String prompt) {
        QbLlmCall call = new QbLlmCall();
        call.setBizType(bizType);
        call.setBizId(bizId);
        call.setModelName(qwenProperties.getModel());
        call.setPromptText(prompt);
        call.setCallStatus(0);
        llmCallMapper.insert(call);

        String apiKey = qwenProperties.resolveApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            call.setResponseText("Qwen api-key not configured");
            call.setResponseJson("{\"error\":\"apiKey missing\"}");
            call.setCallStatus(2);
            call.setLatencyMs(0);
            call.setTokensPrompt(0);
            call.setTokensCompletion(0);
            call.setCostAmount(BigDecimal.ZERO);
            llmCallMapper.updateResponse(call);
            return call;
        }

        long start = System.currentTimeMillis();
        try {
            String baseUrl = qwenProperties.getBaseUrl();
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }

            ObjectNode req = objectMapper.createObjectNode();
            req.put("model", qwenProperties.getModel());
            if (qwenProperties.getTemperature() != null) {
                req.put("temperature", qwenProperties.getTemperature());
            }

            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode sys = objectMapper.createObjectNode();
            sys.put("role", "system");
            sys.put("content", "你是题库管理系统的智能助教。请严格按用户指令输出。若需要结构化输出请用JSON。\n");
            messages.add(sys);

            ObjectNode user = objectMapper.createObjectNode();
            user.put("role", "user");
            user.put("content", prompt);
            messages.add(user);

            req.set("messages", messages);

            String body = objectMapper.writeValueAsString(req);

            HttpRequest httpReq = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp = httpClient.send(httpReq, HttpResponse.BodyHandlers.ofString());
            long latency = System.currentTimeMillis() - start;

            call.setLatencyMs((int) latency);
            call.setResponseText(resp.body());

            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                String content = extractContent(resp.body());
                ObjectNode compact = objectMapper.createObjectNode();
                compact.put("content", content);

                // usage (optional)
                try {
                    JsonNode root = objectMapper.readTree(resp.body());
                    JsonNode usage = root.get("usage");
                    if (usage != null) {
                        compact.set("usage", usage);
                        if (usage.get("prompt_tokens") != null) {
                            call.setTokensPrompt(usage.get("prompt_tokens").asInt());
                        }
                        if (usage.get("completion_tokens") != null) {
                            call.setTokensCompletion(usage.get("completion_tokens").asInt());
                        }
                    }
                } catch (Exception ignore) {
                }

                call.setResponseJson(objectMapper.writeValueAsString(compact));
                call.setCallStatus(1);
                if (call.getTokensPrompt() == null) call.setTokensPrompt(0);
                if (call.getTokensCompletion() == null) call.setTokensCompletion(0);
                call.setCostAmount(BigDecimal.ZERO);
            } else {
                call.setCallStatus(2);
                call.setResponseJson("{\"httpStatus\":" + resp.statusCode() + "}");
                call.setTokensPrompt(0);
                call.setTokensCompletion(0);
                call.setCostAmount(BigDecimal.ZERO);
            }

            llmCallMapper.updateResponse(call);
            return call;
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            log.warn("Qwen call failed", e);
            call.setLatencyMs((int) latency);
            call.setResponseText(e.getMessage());
            call.setResponseJson("{\"error\":\"exception\"}");
            call.setCallStatus(2);
            call.setTokensPrompt(0);
            call.setTokensCompletion(0);
            call.setCostAmount(BigDecimal.ZERO);
            llmCallMapper.updateResponse(call);
            return call;
        }
    }

    @Override
    public String extractContent(String responseText) {
        try {
            JsonNode root = objectMapper.readTree(responseText);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode msg = choices.get(0).get("message");
                if (msg != null && msg.get("content") != null) {
                    return msg.get("content").asText();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
