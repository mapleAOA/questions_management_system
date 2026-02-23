package com.xyz.question_bank_management_system.common.enums;

import lombok.Getter;

@Getter
public enum QuestionStatusEnum {
    DRAFT(1),
    PUBLISHED(2),
    ARCHIVED(3);

    private final int code;

    QuestionStatusEnum(int code) {
        this.code = code;
    }
}
