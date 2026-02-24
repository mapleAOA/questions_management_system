package com.xyz.question_bank_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "username cannot be empty")
    private String username;

    @NotBlank(message = "password cannot be empty")
    private String password;

    @NotBlank(message = "role cannot be empty")
    private String role;

    private String displayName;

    private String email;
}
