package com.xyz.question_bank_management_system.service;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.vo.TeacherAnswerEvidenceVO;
import com.xyz.question_bank_management_system.vo.TeacherAssignmentScoreItemVO;
import com.xyz.question_bank_management_system.vo.TeacherReviewAnswerItemVO;

import java.util.List;

public interface TeacherReviewService {

    PageResponse<TeacherReviewAnswerItemVO> reviewAnswers(Long assignmentId, Boolean needsReview, long page, long size);

    TeacherAnswerEvidenceVO evidence(Long answerId);

    void manualGrade(Long answerId, Integer score, String comment, Long reviewerId);

    List<Long> llmRetry(Long answerId, String modelName, Double temperature, Integer times);

    PageResponse<TeacherAssignmentScoreItemVO> assignmentScores(Long assignmentId, long page, long size);
}
