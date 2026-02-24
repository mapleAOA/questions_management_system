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

    void update(Long questionId, QuestionUpsertRequest request);

    void delete(Long questionId);

    QuestionDetailVO detail(Long questionId);

    PageResponse<QuestionListItemVO> search(QuestionSearchQuery query, long page, long size);

    void publish(Long questionId);

    Long generateAnalysisByLlm(Long questionId);

    List<QbQuestionCase> listCases(Long questionId);

    Long upsertCase(Long questionId, QuestionCaseUpsertRequest request);

    void deleteCase(Long caseId);
}
