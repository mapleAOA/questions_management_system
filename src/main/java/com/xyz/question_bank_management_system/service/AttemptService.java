package com.xyz.question_bank_management_system.service;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.dto.SaveAnswerDraftRequest;
import com.xyz.question_bank_management_system.vo.*;

import java.util.List;

public interface AttemptService {

    AttemptStartVO startAssignmentAttempt(Long assignmentId, Long userId);

    List<AttemptQuestionVO> getAttemptQuestions(Long attemptId, Long userId);

    void saveDraft(Long answerId, Long userId, SaveAnswerDraftRequest request);

    void submitAttempt(Long attemptId, Long userId);

    AttemptResultVO result(Long attemptId, Long userId);

    PageResponse<?> myAttempts(long page, long size, Long userId);
}
