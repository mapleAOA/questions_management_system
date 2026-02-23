package com.xyz.question_bank_management_system.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AdminUpdateUserRolesRequest {
    @NotEmpty(message = "roles不能为空")
    private List<String> roles;
}
