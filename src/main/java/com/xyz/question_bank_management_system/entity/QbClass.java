package com.xyz.question_bank_management_system.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QbClass {
    private Long id;
    private String className;
    private String classCode;
    private String classDesc;
    private Long teacherId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
