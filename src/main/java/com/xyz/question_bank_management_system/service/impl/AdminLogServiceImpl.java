package com.xyz.question_bank_management_system.service.impl;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.mapper.SysAuditLogMapper;
import com.xyz.question_bank_management_system.mapper.SysLoginLogMapper;
import com.xyz.question_bank_management_system.service.AdminLogService;
import com.xyz.question_bank_management_system.vo.AdminAuditLogItemVO;
import com.xyz.question_bank_management_system.util.PageParamUtil;
import com.xyz.question_bank_management_system.vo.AdminLoginLogItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminLogServiceImpl implements AdminLogService {

    private final SysAuditLogMapper sysAuditLogMapper;
    private final SysLoginLogMapper sysLoginLogMapper;

    @Override
    public PageResponse<AdminLoginLogItemVO> pageLoginLogs(String username,
                                                           Boolean successFlag,
                                                           LocalDateTime startTime,
                                                           LocalDateTime endTime,
                                                           long page,
                                                           long size) {
        long safePage = PageParamUtil.normalizePage(page);
        long safeSize = PageParamUtil.normalizeSize(size);
        long offset = PageParamUtil.offset(safePage, safeSize);
        String safeUsername = username == null ? null : username.trim();
        if (safeUsername != null && safeUsername.isEmpty()) {
            safeUsername = null;
        }
        Integer successFlagInt = successFlag == null ? null : (successFlag ? 1 : 0);

        List<AdminLoginLogItemVO> rows = sysLoginLogMapper.pageByFilter(
                safeUsername, successFlagInt, startTime, endTime, offset, safeSize
        );
        long total = sysLoginLogMapper.countByFilter(safeUsername, successFlagInt, startTime, endTime);
        return PageResponse.of(safePage, safeSize, total, rows);
    }

    @Override
    public PageResponse<AdminAuditLogItemVO> pageAuditLogs(String username,
                                                           String action,
                                                           String entityType,
                                                           LocalDateTime startTime,
                                                           LocalDateTime endTime,
                                                           long page,
                                                           long size) {
        long safePage = PageParamUtil.normalizePage(page);
        long safeSize = PageParamUtil.normalizeSize(size);
        long offset = PageParamUtil.offset(safePage, safeSize);

        String safeUsername = username == null ? null : username.trim();
        if (safeUsername != null && safeUsername.isEmpty()) {
            safeUsername = null;
        }
        String safeAction = action == null ? null : action.trim();
        if (safeAction != null && safeAction.isEmpty()) {
            safeAction = null;
        }
        String safeEntityType = entityType == null ? null : entityType.trim();
        if (safeEntityType != null && safeEntityType.isEmpty()) {
            safeEntityType = null;
        }

        List<AdminAuditLogItemVO> rows = sysAuditLogMapper.pageByFilter(
                safeUsername, safeAction, safeEntityType, startTime, endTime, offset, safeSize
        );
        long total = sysAuditLogMapper.countByFilter(
                safeUsername, safeAction, safeEntityType, startTime, endTime
        );
        return PageResponse.of(safePage, safeSize, total, rows);
    }
}
