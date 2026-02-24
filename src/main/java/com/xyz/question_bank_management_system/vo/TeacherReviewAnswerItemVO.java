package com.xyz.question_bank_management_system.vo;

import lombok.Data;

@Data
public class TeacherReviewAnswerItemVO {
    private Long answerId;
    private Long attemptId;
    private Long studentId;
    private Long questionId;
    private Integer questionType;
    private Integer score;
    private Integer currentFinalScore;
    private Integer needsReview;
}
