package com.xyz.question_bank_management_system.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserListItemVO {
    private Long id;
    private String username;
    private String displayName;
    private String email;
    private Integer status;
    private LocalDateTime createdAt;
    private List<String> roles;
}
