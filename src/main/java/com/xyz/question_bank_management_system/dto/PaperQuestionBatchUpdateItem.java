package com.xyz.question_bank_management_system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaperQuestionBatchUpdateItem {
    @NotNull(message = "paperQuestionId不能为空")
    private Long id;

    @NotNull(message = "orderNo不能为空")
    @Min(value = 1, message = "orderNo必须大于等于1")
    private Integer orderNo;

    @NotNull(message = "score不能为空")
    @Min(value = 0, message = "score必须大于等于0")
    private Integer score;
}
