package com.xyz.question_bank_management_system.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QbReply {
    private Long id;
    private Long postId;
    private Long authorId;
    private Long parentReplyId;
    private String content;
    private LocalDateTime createdAt;
    private Integer isDeleted;
}
