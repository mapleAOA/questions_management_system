package com.xyz.question_bank_management_system.dto;

import lombok.Data;

@Data
public class QuestionCaseDTO {
    private Long id;
    private Integer caseNo;
    private String inputData;
    private String expectedOutput;
    private Integer caseScore;
    private Integer isSample;
}
