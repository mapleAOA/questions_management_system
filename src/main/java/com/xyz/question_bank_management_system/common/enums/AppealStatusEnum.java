package com.xyz.question_bank_management_system.common.enums;

import lombok.Getter;

@Getter
public enum AppealStatusEnum {
    PENDING(1),
    APPROVED(2),
    REJECTED(3),
    RESOLVED(4);

    private final int code;

    AppealStatusEnum(int code) {
        this.code = code;
    }
}
