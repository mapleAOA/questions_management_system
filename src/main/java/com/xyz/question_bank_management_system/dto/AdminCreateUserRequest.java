package com.xyz.question_bank_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AdminCreateUserRequest {
    @NotBlank(message = "username不能为空")
    private String username;
    @NotBlank(message = "password不能为空")
    private String password;
    private String displayName;
    private String email;
    /** 1=active,0=disabled */
    private Integer status = 1;
    @NotEmpty(message = "roles不能为空")
    private List<String> roles;
}
