package com.xyz.question_bank_management_system.service;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.dto.PaperAddQuestionRequest;
import com.xyz.question_bank_management_system.dto.PaperQuestionUpdateRequest;
import com.xyz.question_bank_management_system.dto.PaperUpsertRequest;
import com.xyz.question_bank_management_system.entity.QbPaper;
import com.xyz.question_bank_management_system.vo.PaperDetailVO;

public interface PaperService {

    Long create(PaperUpsertRequest request, Long creatorId);

    void update(Long paperId, PaperUpsertRequest request);

    void delete(Long paperId);

    PageResponse<QbPaper> page(long page, long size);

    PaperDetailVO detail(Long paperId);

    Long addQuestion(Long paperId, PaperAddQuestionRequest request);

    void updatePaperQuestion(Long paperQuestionId, PaperQuestionUpdateRequest request);

    void removePaperQuestion(Long paperQuestionId);

    void recalculateTotalScore(Long paperId);
}
