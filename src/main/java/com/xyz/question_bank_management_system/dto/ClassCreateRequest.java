package com.xyz.question_bank_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClassCreateRequest {
    @NotBlank(message = "className cannot be empty")
    private String className;

    private String classDesc;
}
