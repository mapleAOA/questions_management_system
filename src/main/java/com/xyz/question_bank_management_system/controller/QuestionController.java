package com.xyz.question_bank_management_system.controller;

import com.xyz.question_bank_management_system.common.ApiResponse;
import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.common.enums.QuestionTypeEnum;
import com.xyz.question_bank_management_system.common.enums.QuestionStatusEnum;
import com.xyz.question_bank_management_system.dto.QuestionCaseUpsertRequest;
import com.xyz.question_bank_management_system.dto.QuestionSearchQuery;
import com.xyz.question_bank_management_system.dto.QuestionUpsertRequest;
import com.xyz.question_bank_management_system.entity.QbQuestionCase;
import com.xyz.question_bank_management_system.exception.BizException;
import com.xyz.question_bank_management_system.exception.ErrorCode;
import com.xyz.question_bank_management_system.mapper.QbClassMemberMapper;
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
    private final QbClassMemberMapper classMemberMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Long> create(@RequestBody @Valid QuestionUpsertRequest request) {
        Long uid = SecurityContextUtil.getUserId();
        return ApiResponse.ok(questionService.create(request, uid));
    }

    @PutMapping("/{questionId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Void> update(@PathVariable Long questionId, @RequestBody @Valid QuestionUpsertRequest request) {
        Long uid = SecurityContextUtil.getUserId();
        boolean isAdmin = hasRole("ROLE_ADMIN");
        questionService.update(questionId, request, uid, isAdmin);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{questionId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long questionId) {
        Long uid = SecurityContextUtil.getUserId();
        boolean isAdmin = hasRole("ROLE_ADMIN");
        questionService.delete(questionId, uid, isAdmin);
        return ApiResponse.ok();
    }

    @GetMapping("/{questionId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<QuestionDetailVO> detail(@PathVariable Long questionId) {
        Long uid = SecurityContextUtil.getUserId();
        boolean isAdmin = hasRole("ROLE_ADMIN");
        return ApiResponse.ok(questionService.detail(questionId, uid, isAdmin));
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
            @RequestParam(required = false, defaultValue = "all") String source,
            @RequestParam(required = false, defaultValue = "false") Boolean studentView,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size
    ) {
        QuestionSearchQuery q = new QuestionSearchQuery();
        q.setKeyword(keyword);
        q.setChapter(chapter);
        q.setDifficulty(difficulty);
        q.setQuestionType(normalizeEnabledQuestionType(questionType));
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
        Long uid = SecurityContextUtil.getUserId();
        boolean isAdmin = hasRole("ROLE_ADMIN");
        boolean isTeacher = hasRole("ROLE_TEACHER");
        boolean forceStudentView = Boolean.TRUE.equals(studentView);

        if (forceStudentView || (!isTeacher && !isAdmin)) {
            q.setStatus(QuestionStatusEnum.PUBLISHED.getCode());
            q.setViewerId(uid);
            q.setStudentScope(true);
            q.setVisibleTeacherIds(classMemberMapper.listTeacherIdsByStudentId(uid));
        } else if (isTeacher && !isAdmin) {
            q.setTeacherScope(true);
            q.setViewerId(uid);
            q.setSourceType(normalizeSource(source));
        }
        return ApiResponse.ok(questionService.search(q, page, size));
    }

    @PostMapping("/{questionId}/publish")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Void> publish(@PathVariable Long questionId) {
        Long uid = SecurityContextUtil.getUserId();
        boolean isAdmin = hasRole("ROLE_ADMIN");
        questionService.publish(questionId, uid, isAdmin);
        return ApiResponse.ok();
    }

    @PostMapping("/{questionId}/analysis/llm")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Long> llmAnalysis(@PathVariable Long questionId) {
        Long uid = SecurityContextUtil.getUserId();
        boolean isAdmin = hasRole("ROLE_ADMIN");
        return ApiResponse.ok(questionService.generateAnalysisByLlm(questionId, uid, isAdmin));
    }

    @GetMapping("/{questionId}/cases")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<List<QbQuestionCase>> listCases(@PathVariable Long questionId) {
        Long uid = SecurityContextUtil.getUserId();
        boolean isAdmin = hasRole("ROLE_ADMIN");
        return ApiResponse.ok(questionService.listCases(questionId, uid, isAdmin));
    }

    @PostMapping("/{questionId}/cases")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Map<String, Long>> upsertCase(@PathVariable Long questionId,
                                                      @RequestBody @Valid QuestionCaseUpsertRequest request) {
        Long uid = SecurityContextUtil.getUserId();
        boolean isAdmin = hasRole("ROLE_ADMIN");
        Long caseId = questionService.upsertCase(questionId, request, uid, isAdmin);
        Map<String, Long> data = new HashMap<>();
        data.put("caseId", caseId);
        return ApiResponse.ok(data);
    }

    @DeleteMapping("/cases/{caseId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Void> deleteCase(@PathVariable Long caseId) {
        Long uid = SecurityContextUtil.getUserId();
        boolean isAdmin = hasRole("ROLE_ADMIN");
        questionService.deleteCase(caseId, uid, isAdmin);
        return ApiResponse.ok();
    }

    private boolean hasAnyRole(String... roles) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> Arrays.asList(roles).contains(a));
    }

    private boolean hasRole(String role) {
        return hasAnyRole(role);
    }

    private String normalizeSource(String source) {
        if (source == null || source.isBlank()) {
            return "all";
        }
        String normalized = source.trim().toLowerCase();
        if ("all".equals(normalized) || "mine".equals(normalized) || "bank".equals(normalized)) {
            return normalized;
        }
        throw BizException.of(ErrorCode.PARAM_ERROR, "source must be one of: all, mine, bank");
    }

    private Integer normalizeEnabledQuestionType(Integer questionType) {
        if (questionType == null) {
            return null;
        }
        QuestionTypeEnum type = QuestionTypeEnum.of(questionType);
        if (type == null || !type.isEnabledNow()) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "questionType is disabled or invalid: " + questionType);
        }
        return questionType;
    }
}
