package com.xyz.question_bank_management_system.service;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.vo.AdminAuditLogItemVO;
import com.xyz.question_bank_management_system.vo.AdminLoginLogItemVO;

import java.time.LocalDateTime;

public interface AdminLogService {

    PageResponse<AdminAuditLogItemVO> pageAuditLogs(Long userId,
                                                    String action,
                                                    String entityType,
                                                    Long entityId,
                                                    LocalDateTime startTime,
                                                    LocalDateTime endTime,
                                                    long page,
                                                    long size);

    PageResponse<AdminLoginLogItemVO> pageLoginLogs(String username,
                                                    Boolean successFlag,
                                                    LocalDateTime startTime,
                                                    LocalDateTime endTime,
                                                    long page,
                                                    long size);
}
