package com.xyz.question_bank_management_system.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class TextRepairUtil {

    private static final Charset GBK = Charset.forName("GBK");
    private static final String CHINESE_HINTS =
            "\u7684\u4e00\u662f\u5728\u4e0d\u4e86\u6709\u548c\u9898\u76ee\u4f5c\u4e1a\u7b54\u6848\u5b66\u751f\u6559\u5e08\u89e3\u6790\u8bc4\u5206\u63d0\u4ea4\u52a0\u8f7d\u7ed3\u679c\u7cfb\u7edf\u7ba1\u7406\u8bfe\u7a0b\u8f93\u5165\u8f93\u51fa\u6b63\u786e\u9519\u8bef\u6210\u529f\u5931\u8d25";
    private static final String CHINESE_PUNCTUATION =
            "\uff0c\u3002\uff1b\uff1a\uff01\uff1f\uff08\uff09\u300a\u300b\u201c\u201d\u3010\u3011";

    private TextRepairUtil() {
    }

    public static String repairGbkUtf8Mojibake(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        String repaired = tryRepair(text);
        if (repaired == null || repaired.isBlank()) {
            return text;
        }
        return looksLikeMojibake(text, repaired) ? repaired : text;
    }

    public static boolean looksLikeMojibake(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String repaired = tryRepair(text);
        if (repaired == null || repaired.equals(text)) {
            return false;
        }
        return looksLikeMojibake(text, repaired);
    }

    private static boolean looksLikeMojibake(String original, String repaired) {
        return readabilityScore(repaired) >= readabilityScore(original) + 2;
    }

    private static String tryRepair(String text) {
        try {
            return new String(text.getBytes(GBK), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private static int readabilityScore(String text) {
        int score = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (CHINESE_HINTS.indexOf(c) >= 0) {
                score += 2;
                continue;
            }
            if (CHINESE_PUNCTUATION.indexOf(c) >= 0) {
                score += 1;
                continue;
            }
            if (c == '\uFFFD') {
                score -= 3;
                continue;
            }
            if (Character.isISOControl(c) && !Character.isWhitespace(c)) {
                score -= 1;
            }
        }
        return score;
    }
}
