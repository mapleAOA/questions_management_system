package com.xyz.question_bank_management_system.common.enums;

import lombok.Getter;

@Getter
public enum QuestionBankReviewStatusEnum {
    PRIVATE_ONLY(0),
    PENDING(1),
    APPROVED(2),
    REJECTED(3);

    private final int code;

    QuestionBankReviewStatusEnum(int code) {
        this.code = code;
    }

    public static QuestionBankReviewStatusEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (QuestionBankReviewStatusEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
