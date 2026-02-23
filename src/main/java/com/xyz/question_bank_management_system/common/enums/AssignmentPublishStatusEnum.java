package com.xyz.question_bank_management_system.common.enums;

import lombok.Getter;

@Getter
public enum AssignmentPublishStatusEnum {
    DRAFT(1),
    PUBLISHED(2),
    CLOSED(3);

    private final int code;

    AssignmentPublishStatusEnum(int code) {
        this.code = code;
    }
}
