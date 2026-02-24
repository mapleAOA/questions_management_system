package com.xyz.question_bank_management_system.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PracticeStartRequest {

    @NotBlank(message = "mode cannot be blank")
    private String mode;

    @Valid
    private Scope scope;

    private Integer totalScore = 100;
    private Long ruleId;

    @Data
    public static class Scope {
        private List<Long> tagIds;
        private List<String> chapters;
        private List<Integer> questionTypes;
    }
}
