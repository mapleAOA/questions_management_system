package com.xyz.question_bank_management_system.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QbQuestionTag {
    private Long questionId;
    private Long tagId;
    private LocalDateTime createdAt;
}
