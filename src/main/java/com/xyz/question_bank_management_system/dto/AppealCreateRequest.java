package com.xyz.question_bank_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AppealCreateRequest {

    @NotNull(message = "answerId cannot be null")
    private Long answerId;

    @NotBlank(message = "reasonText cannot be blank")
    private String reasonText;

    private List<String> attachments;
}
