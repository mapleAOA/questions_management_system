package com.xyz.question_bank_management_system.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class PaperQuestionBatchUpdateRequest {
    @Valid
    @NotEmpty(message = "questions不能为空")
    private List<PaperQuestionBatchUpdateItem> questions;
}
