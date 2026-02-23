package com.xyz.question_bank_management_system.controller;

import com.xyz.question_bank_management_system.common.ApiResponse;
import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.dto.QuestionSearchQuery;
import com.xyz.question_bank_management_system.dto.QuestionUpsertRequest;
import com.xyz.question_bank_management_system.service.QuestionService;
import com.xyz.question_bank_management_system.util.SecurityContextUtil;
import com.xyz.question_bank_management_system.vo.QuestionDetailVO;
import com.xyz.question_bank_management_system.vo.QuestionListItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ApiResponse<Long> create(@RequestBody @Valid QuestionUpsertRequest request) {
        Long uid = SecurityContextUtil.getUserId();
        return ApiResponse.ok(questionService.create(request, uid));
    }

    @PutMapping("/{questionId}")
    public ApiResponse<Void> update(@PathVariable Long questionId, @RequestBody @Valid QuestionUpsertRequest request) {
        questionService.update(questionId, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{questionId}")
    public ApiResponse<Void> delete(@PathVariable Long questionId) {
        questionService.delete(questionId);
        return ApiResponse.ok();
    }

    @GetMapping("/{questionId}")
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
            List<Long> ids = Arrays.stream(tagIds.split(","))
                    .filter(s -> !s.isBlank())
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            q.setTagIds(ids);
        }
        return ApiResponse.ok(questionService.search(q, page, size));
    }

    @PostMapping("/{questionId}/publish")
    public ApiResponse<Void> publish(@PathVariable Long questionId) {
        questionService.publish(questionId);
        return ApiResponse.ok();
    }

    @PostMapping("/{questionId}/analysis/llm")
    public ApiResponse<Long> llmAnalysis(@PathVariable Long questionId) {
        return ApiResponse.ok(questionService.generateAnalysisByLlm(questionId));
    }
}
