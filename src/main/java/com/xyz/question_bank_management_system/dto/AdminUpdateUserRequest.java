package com.xyz.question_bank_management_system.dto;

import lombok.Data;

@Data
public class AdminUpdateUserRequest {
    private String displayName;
    private String email;
    /** 1=active,0=disabled */
    private Integer status;
}
