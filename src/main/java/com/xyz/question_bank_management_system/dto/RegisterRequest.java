package com.xyz.question_bank_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "username cannot be empty")
    private String username;

    @NotBlank(message = "password cannot be empty")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d])\\S{8,20}$",
            message = "password must be 8-20 characters and include letters, numbers, and special characters"
    )
    private String password;

    @NotBlank(message = "role cannot be empty")
    private String role;

    private String displayName;

    private String email;
}
