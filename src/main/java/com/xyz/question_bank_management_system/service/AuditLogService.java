package com.xyz.question_bank_management_system.service;

public interface AuditLogService {

    void record(Long userId,
                String action,
                String entityType,
                Long entityId,
                Object beforeData,
                Object afterData);
}
