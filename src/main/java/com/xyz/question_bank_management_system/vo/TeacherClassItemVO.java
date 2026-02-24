package com.xyz.question_bank_management_system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeacherClassItemVO {
    private Long id;
    private String className;
    private String classCode;
    private String classDesc;
    private LocalDateTime createdAt;
    private Long studentCount;
}
