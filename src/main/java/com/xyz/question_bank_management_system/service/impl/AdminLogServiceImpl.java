package com.xyz.question_bank_management_system.service.impl;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.mapper.SysLoginLogMapper;
import com.xyz.question_bank_management_system.service.AdminLogService;
import com.xyz.question_bank_management_system.util.PageParamUtil;
import com.xyz.question_bank_management_system.vo.AdminLoginLogItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminLogServiceImpl implements AdminLogService {

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
}
