package com.xyz.question_bank_management_system.controller;

import com.xyz.question_bank_management_system.common.ApiResponse;
import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.service.LlmCallQueryService;
import com.xyz.question_bank_management_system.vo.LlmCallDetailVO;
import com.xyz.question_bank_management_system.vo.LlmCallListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/llm/calls")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
public class LlmCallController {

    private final LlmCallQueryService llmCallQueryService;

    @GetMapping
    public ApiResponse<PageResponse<LlmCallListItemVO>> page(
            @RequestParam(required = false) Integer bizType,
            @RequestParam(required = false) Long bizId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size
    ) {
        return ApiResponse.ok(llmCallQueryService.page(bizType, bizId, page, size));
    }

    @GetMapping("/{llmCallId}")
    public ApiResponse<LlmCallDetailVO> detail(@PathVariable Long llmCallId) {
        return ApiResponse.ok(llmCallQueryService.detail(llmCallId));
    }
}
