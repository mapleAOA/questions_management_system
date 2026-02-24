package com.xyz.question_bank_management_system.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class TeacherLlmRetryRequest {

    private String modelName;
    private Double temperature;

    @Min(value = 1, message = "times must be >= 1")
    @Max(value = 5, message = "times must be <= 5")
    private Integer times = 1;
}
