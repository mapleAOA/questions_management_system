package com.xyz.question_bank_management_system.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestionDetailVO {
    private Long id;
    private String title;
    private Integer questionType;
    private Integer difficulty;
    private String chapter;
    private String stem;
    private String standardAnswer;
    private Integer answerFormat;
    private String analysisText;
    private Integer analysisSource;
    private Long analysisLlmCallId;
    private Integer status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<QuestionOptionVO> options;
    private List<Long> tagIds;
    private List<QuestionCaseVO> cases;

    @Data
    public static class QuestionOptionVO {
        private Long id;
        private String optionLabel;
        private String optionContent;
        private Integer isCorrect;
        private Integer sortOrder;
    }

    @Data
    public static class QuestionCaseVO {
        private Long id;
        private Integer caseNo;
        private String inputData;
        private String expectedOutput;
        private Integer caseScore;
        private Integer isSample;
    }
}
