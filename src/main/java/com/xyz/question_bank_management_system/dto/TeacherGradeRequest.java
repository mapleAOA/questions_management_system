package com.xyz.question_bank_management_system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeacherGradeRequest {

    @NotNull(message = "score cannot be null")
    @Min(value = 0, message = "score must be >= 0")
    private Integer score;

    private String comment;
}
