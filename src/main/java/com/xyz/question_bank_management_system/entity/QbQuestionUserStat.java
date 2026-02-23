package com.xyz.question_bank_management_system.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QbQuestionUserStat {
    private Long userId;
    private Long questionId;
    private Integer attemptCount;
    private Integer correctCount;
    private LocalDateTime lastAttemptAt;
}
