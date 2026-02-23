package com.xyz.question_bank_management_system.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestionListItemVO {
    private Long id;
    private String title;
    private Integer questionType;
    private Integer difficulty;
    private String chapter;
    private Integer status;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private List<Long> tagIds;
}
