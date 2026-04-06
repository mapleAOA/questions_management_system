package com.xyz.question_bank_management_system.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QbPaper {
    private Long id;
    private String paperTitle;
    private String paperDesc;
    /** paper type */
    private Integer paperType;
    private Integer totalScore;
    /** 1=draft,2=published,3=archived */
    private Integer status;
    private Long creatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
