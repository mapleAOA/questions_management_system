package com.xyz.question_bank_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaperUpsertRequest {
    @NotBlank(message = "试卷标题不能为空")
    private String paperTitle;
    private String paperDesc;
    /** paper type */
    @NotNull(message = "paperType不能为空")
    private Integer paperType;
    /** 1=draft,2=published,3=archived */
    private Integer status = 1;
}
