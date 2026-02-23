package com.xyz.question_bank_management_system.controller;

import com.xyz.question_bank_management_system.common.ApiResponse;
import com.xyz.question_bank_management_system.entity.*;
import com.xyz.question_bank_management_system.service.StatsService;
import com.xyz.question_bank_management_system.util.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/wrong-questions")
    public ApiResponse<List<QbWrongQuestion>> wrongQuestions() {
        Long uid = SecurityContextUtil.getUserId();
        return ApiResponse.ok(statsService.wrongQuestions(uid));
    }

    @PostMapping("/wrong-questions/{questionId}/resolve")
    public ApiResponse<Void> resolve(@PathVariable Long questionId) {
        Long uid = SecurityContextUtil.getUserId();
        statsService.resolveWrongQuestion(uid, questionId);
        return ApiResponse.ok();
    }

    @GetMapping("/mastery")
    public ApiResponse<List<QbTagMastery>> mastery() {
        Long uid = SecurityContextUtil.getUserId();
        return ApiResponse.ok(statsService.mastery(uid));
    }

    @GetMapping("/ability")
    public ApiResponse<QbUserAbility> ability() {
        Long uid = SecurityContextUtil.getUserId();
        return ApiResponse.ok(statsService.ability(uid));
    }

    @GetMapping("/question-stats")
    public ApiResponse<List<QbQuestionUserStat>> questionStats() {
        Long uid = SecurityContextUtil.getUserId();
        return ApiResponse.ok(statsService.questionStats(uid));
    }
}
