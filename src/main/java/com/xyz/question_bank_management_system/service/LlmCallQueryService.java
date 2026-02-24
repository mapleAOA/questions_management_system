package com.xyz.question_bank_management_system.service;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.vo.LlmCallDetailVO;
import com.xyz.question_bank_management_system.vo.LlmCallListItemVO;

public interface LlmCallQueryService {

    PageResponse<LlmCallListItemVO> page(Integer bizType, Long bizId, long page, long size);

    LlmCallDetailVO detail(Long llmCallId);
}
