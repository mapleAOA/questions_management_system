package com.xyz.question_bank_management_system.service;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.dto.QuestionCaseUpsertRequest;
import com.xyz.question_bank_management_system.dto.QuestionSearchQuery;
import com.xyz.question_bank_management_system.dto.QuestionUpsertRequest;
import com.xyz.question_bank_management_system.entity.QbQuestionCase;
import com.xyz.question_bank_management_system.vo.QuestionDetailVO;
import com.xyz.question_bank_management_system.vo.QuestionListItemVO;

import java.util.List;

public interface QuestionService {

    Long create(QuestionUpsertRequest request, Long creatorId);

    void update(Long questionId, QuestionUpsertRequest request, Long actorId, boolean isAdmin);

    void delete(Long questionId, Long actorId, boolean isAdmin);

    QuestionDetailVO detail(Long questionId, Long actorId, boolean isAdmin);

    PageResponse<QuestionListItemVO> search(QuestionSearchQuery query, long page, long size);

    void publish(Long questionId, Long actorId, boolean isAdmin);

    Long generateAnalysisByLlm(Long questionId, Long actorId, boolean isAdmin);

    List<QbQuestionCase> listCases(Long questionId, Long actorId, boolean isAdmin);

    Long upsertCase(Long questionId, QuestionCaseUpsertRequest request, Long actorId, boolean isAdmin);

    void deleteCase(Long caseId, Long actorId, boolean isAdmin);
}
