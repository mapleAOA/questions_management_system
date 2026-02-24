package com.xyz.question_bank_management_system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionCaseUpsertRequest {

    @NotNull(message = "caseNo cannot be null")
    @Min(value = 1, message = "caseNo must be >= 1")
    private Integer caseNo;

    @NotBlank(message = "inputData cannot be blank")
    private String inputData;

    @NotBlank(message = "expectedOutput cannot be blank")
    private String expectedOutput;

    @Min(value = 0, message = "caseScore must be >= 0")
    private Integer caseScore;

    private Boolean isSample;
}
