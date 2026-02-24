package com.xyz.question_bank_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinClassRequest {
    @NotBlank(message = "classCode cannot be empty")
    private String classCode;
}
