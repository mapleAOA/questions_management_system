package com.xyz.question_bank_management_system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminAuditLogItemVO {
    private Long logId;
    private Long userId;
    private String action;
    private String entityType;
    private Long entityId;
    private LocalDateTime createdAt;
}
