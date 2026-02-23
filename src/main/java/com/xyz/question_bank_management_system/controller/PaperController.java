package com.xyz.question_bank_management_system.controller;

import com.xyz.question_bank_management_system.common.ApiResponse;
import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.dto.PaperAddQuestionRequest;
import com.xyz.question_bank_management_system.dto.PaperQuestionUpdateRequest;
import com.xyz.question_bank_management_system.dto.PaperUpsertRequest;
import com.xyz.question_bank_management_system.entity.QbPaper;
import com.xyz.question_bank_management_system.service.PaperService;
import com.xyz.question_bank_management_system.util.SecurityContextUtil;
import com.xyz.question_bank_management_system.vo.PaperDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/papers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
public class PaperController {

    private final PaperService paperService;

    @PostMapping
    public ApiResponse<Long> create(@RequestBody @Valid PaperUpsertRequest request) {
        Long uid = SecurityContextUtil.getUserId();
        return ApiResponse.ok(paperService.create(request, uid));
    }

    @PutMapping("/{paperId}")
    public ApiResponse<Void> update(@PathVariable Long paperId, @RequestBody @Valid PaperUpsertRequest request) {
        paperService.update(paperId, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{paperId}")
    public ApiResponse<Void> delete(@PathVariable Long paperId) {
        paperService.delete(paperId);
        return ApiResponse.ok();
    }

    @GetMapping
    public ApiResponse<PageResponse<QbPaper>> page(@RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "20") long size) {
        return ApiResponse.ok(paperService.page(page, size));
    }

    @GetMapping("/{paperId}")
    public ApiResponse<PaperDetailVO> detail(@PathVariable Long paperId) {
        return ApiResponse.ok(paperService.detail(paperId));
    }

    @PostMapping("/{paperId}/questions")
    public ApiResponse<Long> addQuestion(@PathVariable Long paperId, @RequestBody @Valid PaperAddQuestionRequest request) {
        return ApiResponse.ok(paperService.addQuestion(paperId, request));
    }

    @PutMapping("/questions/{paperQuestionId}")
    public ApiResponse<Void> updatePaperQuestion(@PathVariable Long paperQuestionId, @RequestBody @Valid PaperQuestionUpdateRequest request) {
        paperService.updatePaperQuestion(paperQuestionId, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/questions/{paperQuestionId}")
    public ApiResponse<Void> removePaperQuestion(@PathVariable Long paperQuestionId) {
        paperService.removePaperQuestion(paperQuestionId);
        return ApiResponse.ok();
    }

    @PostMapping("/{paperId}/recalculate")
    public ApiResponse<Void> recalculate(@PathVariable Long paperId) {
        paperService.recalculateTotalScore(paperId);
        return ApiResponse.ok();
    }
}
