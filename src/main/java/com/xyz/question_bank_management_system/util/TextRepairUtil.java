package com.xyz.question_bank_management_system.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class TextRepairUtil {

    private static final Charset GBK = Charset.forName("GBK");

    private static final String[] MOJIBAKE_TOKENS = {
            "鍔犺浇", "鏃犳潈", "璇锋眰", "澶辫触", "绛旀", "浣滀笟",
            "棰樼洰", "绫诲瀷", "鐘舵€", "鎻愪氦", "鍙栨秷", "缁撴灉",
            "宸叉彁浜", "閿欒", "璁板綍", "璇峰～", "纭", "锛", "銆"
    };

    private static final String MOJIBAKE_CHARS = "鍔浣璇鏌鎻缁缃鏃绛锛銆鍙鏍姝閿闂鎬鎺纭璁闄";

    private TextRepairUtil() {
    }

    public static String repairGbkUtf8Mojibake(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        if (!looksLikeMojibake(text)) {
            return text;
        }
        try {
            String repaired = new String(text.getBytes(GBK), StandardCharsets.UTF_8);
            return repaired == null || repaired.isBlank() ? text : repaired;
        } catch (Exception e) {
            return text;
        }
    }

    public static boolean looksLikeMojibake(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        for (String token : MOJIBAKE_TOKENS) {
            if (text.contains(token)) {
                return true;
            }
        }
        for (int i = 0; i < text.length(); i++) {
            if (MOJIBAKE_CHARS.indexOf(text.charAt(i)) >= 0) {
                return true;
            }
        }
        return false;
    }
}
