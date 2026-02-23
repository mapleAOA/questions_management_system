package com.xyz.question_bank_management_system.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AssignmentTargetsRequest {
    @NotEmpty(message = "userIds不能为空")
    private List<Long> userIds;
}
