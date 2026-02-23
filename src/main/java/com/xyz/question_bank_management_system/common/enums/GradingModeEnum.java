package com.xyz.question_bank_management_system.common.enums;

import lombok.Getter;

@Getter
public enum GradingModeEnum {
    AUTO(1),
    LLM(2),
    MANUAL(3);

    private final int code;

    GradingModeEnum(int code) {
        this.code = code;
    }
}
