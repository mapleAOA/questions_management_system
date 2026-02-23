package com.xyz.question_bank_management_system.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionSearchQuery {
    private String keyword;
    private String chapter;
    private Integer difficulty;
    private Integer questionType;
    private Integer status;
    /** 单个标签过滤 */
    private Long tagId;
    /** 多个标签过滤（AND/OR 逻辑由 service 决定，当前用 OR） */
    private List<Long> tagIds;
    /** 创建人过滤 */
    private Long createdBy;
}
