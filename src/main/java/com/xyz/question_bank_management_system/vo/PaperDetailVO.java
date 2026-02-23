package com.xyz.question_bank_management_system.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PaperDetailVO {
    private Long id;
    private String paperTitle;
    private String paperDesc;
    private Integer paperType;
    private Integer totalScore;
    private String ruleJson;
    private Integer status;
    private Long creatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<PaperQuestionVO> questions;

    @Data
    public static class PaperQuestionVO {
        private Long id;
        private Long questionId;
        private Integer orderNo;
        private Integer score;
        private String snapshotJson;
        private String snapshotHash;
    }
}
