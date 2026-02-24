package com.xyz.question_bank_management_system.controller;

import com.xyz.question_bank_management_system.common.ApiResponse;
import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.dto.AppealHandleRequest;
import com.xyz.question_bank_management_system.dto.TeacherGradeRequest;
import com.xyz.question_bank_management_system.dto.TeacherLlmRetryRequest;
import com.xyz.question_bank_management_system.service.AppealService;
import com.xyz.question_bank_management_system.service.TeacherReviewService;
import com.xyz.question_bank_management_system.util.SecurityContextUtil;
import com.xyz.question_bank_management_system.vo.TeacherAnswerEvidenceVO;
import com.xyz.question_bank_management_system.vo.TeacherAppealItemVO;
import com.xyz.question_bank_management_system.vo.TeacherAssignmentScoreItemVO;
import com.xyz.question_bank_management_system.vo.TeacherReviewAnswerItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
public class TeacherReviewController {

    private final TeacherReviewService teacherReviewService;
    private final AppealService appealService;

    @GetMapping("/review/answers")
    public ApiResponse<PageResponse<TeacherReviewAnswerItemVO>> reviewAnswers(
            @RequestParam(required = false) Long assignmentId,
            @RequestParam(defaultValue = "true") Boolean needsReview,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size
    ) {
        return ApiResponse.ok(teacherReviewService.reviewAnswers(assignmentId, needsReview, page, size));
    }

    @GetMapping("/answers/{answerId}/evidence")
    public ApiResponse<TeacherAnswerEvidenceVO> evidence(@PathVariable Long answerId) {
        return ApiResponse.ok(teacherReviewService.evidence(answerId));
    }

    @PostMapping("/answers/{answerId}/grade")
    public ApiResponse<Void> manualGrade(@PathVariable Long answerId, @RequestBody @Valid TeacherGradeRequest request) {
        Long reviewerId = SecurityContextUtil.getUserId();
        teacherReviewService.manualGrade(answerId, request.getScore(), request.getComment(), reviewerId);
        return ApiResponse.ok();
    }

    @PostMapping("/answers/{answerId}/llm-retry")
    public ApiResponse<Map<String, List<Long>>> llmRetry(@PathVariable Long answerId,
                                                          @RequestBody(required = false) @Valid TeacherLlmRetryRequest request) {
        String modelName = request == null ? null : request.getModelName();
        Double temperature = request == null ? null : request.getTemperature();
        Integer times = request == null ? 1 : request.getTimes();
        List<Long> llmCallIds = teacherReviewService.llmRetry(answerId, modelName, temperature, times);
        return ApiResponse.ok("已触发", Map.of("llmCallIds", llmCallIds));
    }

    @GetMapping("/assignments/{assignmentId}/scores")
    public ApiResponse<PageResponse<TeacherAssignmentScoreItemVO>> assignmentScores(@PathVariable Long assignmentId,
                                                                                    @RequestParam(defaultValue = "1") long page,
                                                                                    @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(teacherReviewService.assignmentScores(assignmentId, page, size));
    }

    @GetMapping("/appeals")
    public ApiResponse<PageResponse<TeacherAppealItemVO>> appeals(@RequestParam(required = false) Integer status,
                                                                   @RequestParam(defaultValue = "1") long page,
                                                                   @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(appealService.pageTeacherAppeals(status, page, size));
    }

    @PostMapping("/appeals/{appealId}/handle")
    public ApiResponse<Void> handleAppeal(@PathVariable Long appealId, @RequestBody @Valid AppealHandleRequest request) {
        Long handlerId = SecurityContextUtil.getUserId();
        appealService.handleAppeal(appealId, request, handlerId);
        return ApiResponse.ok();
    }
}
