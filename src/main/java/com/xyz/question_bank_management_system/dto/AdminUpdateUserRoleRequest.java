package com.xyz.question_bank_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminUpdateUserRoleRequest {
    @NotBlank(message = "role不能为空")
    private String role;
}
