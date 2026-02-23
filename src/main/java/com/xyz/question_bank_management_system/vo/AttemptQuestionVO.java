package com.xyz.question_bank_management_system.vo;

import lombok.Data;

@Data
public class AttemptQuestionVO {
    private Long attemptQuestionId;
    private Long questionId;
    private Integer orderNo;
    private Integer score;
    /** 题目快照（JSON字符串），不建议在作答阶段返回标准答案 */
    private String snapshotJson;

    private Long answerId;
    private String answerContent;
    private Integer answerStatus;
}
