package com.xyz.question_bank_management_system.common.enums;

import lombok.Getter;

@Getter
public enum QuestionTypeEnum {
    SINGLE(1),
    MULTIPLE(2),
    TRUE_FALSE(3),
    BLANK(4),
    SHORT(5),
    CODE(6),
    CODE_READING(7);

    private final int code;

    QuestionTypeEnum(int code) {
        this.code = code;
    }

    public static QuestionTypeEnum of(int code) {
        for (QuestionTypeEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }

    /**
     * 是否为客观题（可自动判分）。
     */
    public boolean isObjective() {
        return this == SINGLE || this == MULTIPLE || this == TRUE_FALSE || this == BLANK;
    }

    /**
     * 是否为主观题（需要 LLM/人工）。
     */
    public boolean isSubjective() {
        return !isObjective();
    }
}
