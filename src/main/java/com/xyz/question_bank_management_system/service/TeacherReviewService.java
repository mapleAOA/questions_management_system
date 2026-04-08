package com.xyz.question_bank_management_system.service;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.vo.TeacherAnswerEvidenceVO;
import com.xyz.question_bank_management_system.vo.TeacherAssignmentScoreItemVO;
import com.xyz.question_bank_management_system.vo.TeacherAssignmentStudentDetailVO;
import com.xyz.question_bank_management_system.vo.TeacherAssignmentTargetItemVO;
import com.xyz.question_bank_management_system.vo.TeacherReviewAnswerItemVO;

import java.util.List;

public interface TeacherReviewService {

    PageResponse<TeacherReviewAnswerItemVO> reviewAnswers(Long assignmentId,
                                                          Boolean needsReview,
                                                          long page,
                                                          long size,
                                                          Long actorId,
                                                          boolean isAdmin);

    TeacherAnswerEvidenceVO evidence(Long answerId, Long actorId, boolean isAdmin);

    void manualGrade(Long answerId, Integer score, String comment, Long reviewerId, boolean isAdmin);

    List<Long> llmRetry(Long answerId,
                        String modelName,
                        Double temperature,
                        Integer times,
                        Long actorId,
                        boolean isAdmin);

    PageResponse<TeacherAssignmentScoreItemVO> assignmentScores(Long assignmentId,
                                                                long page,
                                                                long size,
                                                                Long actorId,
                                                                boolean isAdmin);

    PageResponse<TeacherAssignmentTargetItemVO> assignmentTargets(Long assignmentId,
                                                                  long page,
                                                                  long size,
                                                                  Long actorId,
                                                                  boolean isAdmin);

    TeacherAssignmentStudentDetailVO assignmentStudentDetail(Long assignmentId,
                                                             Long studentId,
                                                             Long actorId,
                                                             boolean isAdmin);
}
