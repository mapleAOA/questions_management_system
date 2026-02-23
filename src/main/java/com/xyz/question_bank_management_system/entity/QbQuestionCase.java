package com.xyz.question_bank_management_system.entity;

import lombok.Data;

@Data
public class QbQuestionCase {
    private Long id;
    private Long questionId;
    private Integer caseNo;
    private String inputData;
    private String expectedOutput;
    private Integer caseScore;
    private Integer isSample;
}
