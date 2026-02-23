package com.xyz.question_bank_management_system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaperQuestionUpdateRequest {
    @NotNull(message = "orderNo不能为空")
    private Integer orderNo;
    @NotNull(message = "score不能为空")
    private Integer score;
}
