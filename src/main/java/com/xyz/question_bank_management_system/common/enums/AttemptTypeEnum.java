package com.xyz.question_bank_management_system.common.enums;

import lombok.Getter;

@Getter
public enum AttemptTypeEnum {
    ASSIGNMENT(1),
    PRACTICE(2);

    private final int code;

    AttemptTypeEnum(int code) {
        this.code = code;
    }
}
