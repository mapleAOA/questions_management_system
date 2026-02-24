package com.xyz.question_bank_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class AppealHandleRequest {

    @NotBlank(message = "action cannot be blank")
    private String action;

    @PositiveOrZero(message = "finalScore must be >= 0")
    private Integer finalScore;

    private String decisionComment;
}
