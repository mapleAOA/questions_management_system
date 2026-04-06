package com.xyz.question_bank_management_system.service;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.vo.AdminLoginLogItemVO;

import java.time.LocalDateTime;

public interface AdminLogService {

    PageResponse<AdminLoginLogItemVO> pageLoginLogs(String username,
                                                    Boolean successFlag,
                                                    LocalDateTime startTime,
                                                    LocalDateTime endTime,
                                                    long page,
                                                    long size);
}
