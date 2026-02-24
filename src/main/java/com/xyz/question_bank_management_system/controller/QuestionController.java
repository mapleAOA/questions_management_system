package com.xyz.question_bank_management_system.controller;

import com.xyz.question_bank_management_system.common.ApiResponse;
import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.common.enums.QuestionStatusEnum;
import com.xyz.question_bank_management_system.dto.QuestionCaseUpsertRequest;
import com.xyz.question_bank_management_system.dto.QuestionSearchQuery;
import com.xyz.question_bank_management_system.dto.QuestionUpsertRequest;
import com.xyz.question_bank_management_system.entity.QbQuestionCase;
import com.xyz.question_bank_management_system.exception.BizException;
import com.xyz.question_bank_management_system.exception.ErrorCode;
import com.xyz.question_bank_management_system.service.QuestionService;
import com.xyz.question_bank_management_system.util.SecurityContextUtil;
import com.xyz.question_bank_management_system.vo.QuestionDetailVO;
import com.xyz.question_bank_management_system.vo.QuestionListItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Long> create(@RequestBody @Valid QuestionUpsertRequest request) {
        Long uid = SecurityContextUtil.getUserId();
        return ApiResponse.ok(questionService.create(request, uid));
    }

    @PutMapping("/{questionId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Void> update(@PathVariable Long questionId, @RequestBody @Valid QuestionUpsertRequest request) {
        questionService.update(questionId, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{questionId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long questionId) {
        questionService.delete(questionId);
        return ApiResponse.ok();
    }

    @GetMapping("/{questionId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<QuestionDetailVO> detail(@PathVariable Long questionId) {
        return ApiResponse.ok(questionService.detail(questionId));
    }

    @GetMapping
    public ApiResponse<PageResponse<QuestionListItemVO>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String chapter,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Integer questionType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String tagIds,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size
    ) {
        QuestionSearchQuery q = new QuestionSearchQuery();
        q.setKeyword(keyword);
        q.setChapter(chapter);
        q.setDifficulty(difficulty);
        q.setQuestionType(questionType);
        q.setStatus(status);
        q.setTagId(tagId);
        if (tagIds != null && !tagIds.isBlank()) {
            try {
                List<Long> ids = Arrays.stream(tagIds.split(","))
                        .filter(s -> !s.isBlank())
                        .map(String::trim)
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                q.setTagIds(ids);
            } catch (NumberFormatException e) {
                throw BizException.of(ErrorCode.PARAM_ERROR, "tagIds must be comma-separated numbers");
            }
        }
        if (!hasAnyRole("ROLE_TEACHER", "ROLE_ADMIN")) {
            q.setStatus(QuestionStatusEnum.PUBLISHED.getCode());
        }
        return ApiResponse.ok(questionService.search(q, page, size));
    }

    @PostMapping("/{questionId}/publish")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Void> publish(@PathVariable Long questionId) {
        questionService.publish(questionId);
        return ApiResponse.ok();
    }

    @PostMapping("/{questionId}/analysis/llm")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Long> llmAnalysis(@PathVariable Long questionId) {
        return ApiResponse.ok(questionService.generateAnalysisByLlm(questionId));
    }

    @GetMapping("/{questionId}/cases")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<List<QbQuestionCase>> listCases(@PathVariable Long questionId) {
        return ApiResponse.ok(questionService.listCases(questionId));
    }

    @PostMapping("/{questionId}/cases")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Map<String, Long>> upsertCase(@PathVariable Long questionId,
                                                      @RequestBody @Valid QuestionCaseUpsertRequest request) {
        Long caseId = questionService.upsertCase(questionId, request);
        Map<String, Long> data = new HashMap<>();
        data.put("caseId", caseId);
        return ApiResponse.ok(data);
    }

    @DeleteMapping("/cases/{caseId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Void> deleteCase(@PathVariable Long caseId) {
        questionService.deleteCase(caseId);
        return ApiResponse.ok();
    }

    private boolean hasAnyRole(String... roles) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> Arrays.asList(roles).contains(a));
    }
}
