package com.xyz.question_bank_management_system.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QbAssignmentTarget {
    private Long assignmentId;
    private Long userId;
    private LocalDateTime createdAt;
}
