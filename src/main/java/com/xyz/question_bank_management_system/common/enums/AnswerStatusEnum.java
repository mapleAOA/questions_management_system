package com.xyz.question_bank_management_system.common.enums;

import lombok.Getter;

@Getter
public enum AnswerStatusEnum {
    DRAFT(1),
    SUBMITTED(2);

    private final int code;

    AnswerStatusEnum(int code) {
        this.code = code;
    }
}
