package com.xyz.question_bank_management_system.service;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.dto.QuestionBankReviewRequest;
import com.xyz.question_bank_management_system.dto.QuestionSearchQuery;
import com.xyz.question_bank_management_system.dto.QuestionUpsertRequest;
import com.xyz.question_bank_management_system.vo.QuestionDetailVO;
import com.xyz.question_bank_management_system.vo.QuestionLlmBatchResultVO;
import com.xyz.question_bank_management_system.vo.QuestionListItemVO;

public interface QuestionService {

    Long create(QuestionUpsertRequest request, Long creatorId);

    void update(Long questionId, QuestionUpsertRequest request, Long actorId, boolean isAdmin);

    void delete(Long questionId, Long actorId, boolean isAdmin);

    QuestionDetailVO detail(Long questionId, Long actorId, boolean isAdmin);

    PageResponse<QuestionListItemVO> search(QuestionSearchQuery query, long page, long size);

    void publish(Long questionId, Long actorId, boolean isAdmin);

    void submitForBankReview(Long questionId, Long actorId);

    void cancelBankReview(Long questionId, Long actorId);

    void reviewBankQuestion(Long questionId, QuestionBankReviewRequest request, Long reviewerId);

    QuestionLlmBatchResultVO generateAnalysisByLlm(Long questionId, String providerKey, Long actorId, boolean isAdmin);
}
