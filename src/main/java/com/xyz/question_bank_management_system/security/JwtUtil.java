package com.xyz.question_bank_management_system.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xyz.question_bank_management_system.exception.BizException;
import com.xyz.question_bank_management_system.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final String MSG_INIT_FAILED = "\u521d\u59cb\u5316 JwtUtil \u5931\u8d25";
    private static final String MSG_TOKEN_FORMAT_ERROR = "\u4ee4\u724c\u683c\u5f0f\u9519\u8bef";
    private static final String MSG_TOKEN_SIGN_ERROR = "\u4ee4\u724c\u7b7e\u540d\u65e0\u6548";
    private static final String MSG_TOKEN_EXPIRED = "\u767b\u5f55\u4ee4\u724c\u5df2\u8fc7\u671f";
    private static final String MSG_TOKEN_PARSE_FAILED = "\u4ee4\u724c\u89e3\u6790\u5931\u8d25";

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expire-seconds:7200}")
    private long expireSeconds;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Mac hmacSha256;

    @PostConstruct
    public void init() {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            this.hmacSha256 = mac;
        } catch (Exception e) {
            throw new IllegalStateException(MSG_INIT_FAILED, e);
        }
    }

    public String generateToken(long userId, String username, String role) {
        long now = Instant.now().getEpochSecond();
        long exp = now + expireSeconds;

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("uid", userId);
        payload.put("sub", username);
        payload.put("role", role);
        payload.put("iat", now);
        payload.put("exp", exp);

        String headerB64 = base64Url(json(header));
        String payloadB64 = base64Url(json(payload));
        String signingInput = headerB64 + "." + payloadB64;
        String signatureB64 = base64Url(hmac(signingInput));
        return signingInput + "." + signatureB64;
    }

    public JwtPayload parseAndValidate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw BizException.of(ErrorCode.UNAUTHORIZED, MSG_TOKEN_FORMAT_ERROR);
            }
            String signingInput = parts[0] + "." + parts[1];
            String expectedSig = base64Url(hmac(signingInput));
            if (!constantTimeEquals(expectedSig, parts[2])) {
                throw BizException.of(ErrorCode.UNAUTHORIZED, MSG_TOKEN_SIGN_ERROR);
            }
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(payloadJson, Map.class);

            long exp = ((Number) payload.get("exp")).longValue();
            long now = Instant.now().getEpochSecond();
            if (now >= exp) {
                throw BizException.of(ErrorCode.UNAUTHORIZED, MSG_TOKEN_EXPIRED);
            }

            long uid = ((Number) payload.get("uid")).longValue();
            String sub = String.valueOf(payload.get("sub"));
            String role = payload.get("role") == null ? "" : String.valueOf(payload.get("role"));
            return new JwtPayload(uid, sub, role, exp);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw BizException.of(ErrorCode.UNAUTHORIZED, MSG_TOKEN_PARSE_FAILED);
        }
    }

    private byte[] hmac(String input) {
        try {
            Mac mac = (Mac) hmacSha256.clone();
            return mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
        } catch (CloneNotSupportedException e) {
            synchronized (this) {
                return hmacSha256.doFinal(input.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private String json(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private String base64Url(String s) {
        return base64Url(s.getBytes(StandardCharsets.UTF_8));
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r |= a.charAt(i) ^ b.charAt(i);
        }
        return r == 0;
    }

    public record JwtPayload(long userId, String username, String roleCode, long expEpochSec) {
    }
}
