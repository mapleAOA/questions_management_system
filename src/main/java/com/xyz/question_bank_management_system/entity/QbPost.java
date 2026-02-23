package com.xyz.question_bank_management_system.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QbPost {
    private Long id;
    private Long authorId;
    private String title;
    private String content;
    private Long relatedQuestionId;
    /** 1=normal,2=closed */
    private Integer postStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
