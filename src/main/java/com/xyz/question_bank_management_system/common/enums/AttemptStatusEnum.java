package com.xyz.question_bank_management_system.common.enums;

import lombok.Getter;

@Getter
public enum AttemptStatusEnum {
    IN_PROGRESS(1),
    SUBMITTED(2),
    GRADING(3),
    GRADED(4);

    private final int code;

    AttemptStatusEnum(int code) {
        this.code = code;
    }
}
