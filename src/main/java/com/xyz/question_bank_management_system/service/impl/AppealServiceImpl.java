package com.xyz.question_bank_management_system.service.impl;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.common.enums.AppealStatusEnum;
import com.xyz.question_bank_management_system.common.enums.AttemptStatusEnum;
import com.xyz.question_bank_management_system.common.enums.GradingModeEnum;
import com.xyz.question_bank_management_system.common.enums.QuestionTypeEnum;
import com.xyz.question_bank_management_system.dto.AppealCreateRequest;
import com.xyz.question_bank_management_system.dto.AppealHandleRequest;
import com.xyz.question_bank_management_system.entity.QbAnswer;
import com.xyz.question_bank_management_system.entity.QbAppeal;
import com.xyz.question_bank_management_system.entity.QbAttempt;
import com.xyz.question_bank_management_system.entity.QbAttemptQuestion;
import com.xyz.question_bank_management_system.entity.QbGradingRecord;
import com.xyz.question_bank_management_system.exception.BizException;
import com.xyz.question_bank_management_system.exception.ErrorCode;
import com.xyz.question_bank_management_system.mapper.QbAnswerMapper;
import com.xyz.question_bank_management_system.mapper.QbAppealMapper;
import com.xyz.question_bank_management_system.mapper.QbAttemptMapper;
import com.xyz.question_bank_management_system.mapper.QbAttemptQuestionMapper;
import com.xyz.question_bank_management_system.mapper.QbGradingRecordMapper;
import com.xyz.question_bank_management_system.service.AppealService;
import com.xyz.question_bank_management_system.service.UserAbilityService;
import com.xyz.question_bank_management_system.util.PageParamUtil;
import com.xyz.question_bank_management_system.vo.AppealMyItemVO;
import com.xyz.question_bank_management_system.vo.TeacherAppealItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AppealServiceImpl implements AppealService {

    private final QbAppealMapper appealMapper;
    private final QbAnswerMapper answerMapper;
    private final QbAttemptQuestionMapper attemptQuestionMapper;
    private final QbAttemptMapper attemptMapper;
    private final QbGradingRecordMapper gradingRecordMapper;
    private final UserAbilityService userAbilityService;

    @Override
    @Transactional
    public Long submitAppeal(AppealCreateRequest request, Long userId) {
        QbAnswer answer = answerMapper.selectById(request.getAnswerId());
        if (answer == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "answer not found");
        }
        if (!userId.equals(answer.getUserId())) {
            throw BizException.of(ErrorCode.FORBIDDEN, "cannot appeal others' answer");
        }
        long pendingCount = appealMapper.countPendingByAnswerAndUser(answer.getId(), userId);
        if (pendingCount > 0) {
            throw BizException.of(ErrorCode.CONFLICT, "pending appeal already exists");
        }

        QbAppeal appeal = new QbAppeal();
        appeal.setAnswerId(answer.getId());
        appeal.setUserId(userId);
        appeal.setReasonText(request.getReasonText().trim());
        appeal.setAppealStatus(AppealStatusEnum.PENDING.getCode());
        appealMapper.insert(appeal);
        refreshAttemptReviewState(answer.getAttemptId());
        return appeal.getId();
    }

    @Override
    public PageResponse<AppealMyItemVO> pageMyAppeals(Long userId, Integer status, long page, long size) {
        Integer safeStatus = normalizeStatus(status, false);
        long safePage = PageParamUtil.normalizePage(page);
        long safeSize = PageParamUtil.normalizeSize(size);
        long offset = PageParamUtil.offset(safePage, safeSize);

        List<AppealMyItemVO> rows = appealMapper.pageByUser(userId, safeStatus, offset, safeSize);
        long total = appealMapper.countByUser(userId, safeStatus);
        return PageResponse.of(safePage, safeSize, total, rows);
    }

    @Override
    public PageResponse<TeacherAppealItemVO> pageTeacherAppeals(Integer status, long page, long size) {
        Integer safeStatus = normalizeStatus(status, true);
        long safePage = PageParamUtil.normalizePage(page);
        long safeSize = PageParamUtil.normalizeSize(size);
        long offset = PageParamUtil.offset(safePage, safeSize);

        List<TeacherAppealItemVO> rows = appealMapper.pageForTeacher(safeStatus, offset, safeSize);
        long total = appealMapper.countForTeacher(safeStatus);
        return PageResponse.of(safePage, safeSize, total, rows);
    }

    @Override
    @Transactional
    public void handleAppeal(Long appealId, AppealHandleRequest request, Long handlerId) {
        QbAppeal appeal = appealMapper.selectById(appealId);
        if (appeal == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "appeal not found");
        }
        if (appeal.getAppealStatus() == null || appeal.getAppealStatus() != AppealStatusEnum.PENDING.getCode()) {
            throw BizException.of(ErrorCode.CONFLICT, "appeal has already been handled");
        }

        String action = request.getAction().trim().toLowerCase(Locale.ROOT);
        if (!"approve".equals(action) && !"reject".equals(action)) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "action must be approve or reject");
        }

        QbAnswer answer = answerMapper.selectById(appeal.getAnswerId());
        if (answer == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "answer not found");
        }
        QbAttemptQuestion attemptQuestion = attemptQuestionMapper.selectById(answer.getAttemptQuestionId());
        if (attemptQuestion == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "attempt question not found");
        }

        LocalDateTime now = LocalDateTime.now();
        Integer currentFinalScore = safeInt(answer.getFinalScore());
        Integer targetFinalScore = currentFinalScore;
        int targetAppealStatus;

        if ("reject".equals(action)) {
            targetAppealStatus = AppealStatusEnum.REJECTED.getCode();
        } else {
            if (request.getFinalScore() != null) {
                int maxScore = safeInt(attemptQuestion.getScore());
                if (request.getFinalScore() > maxScore) {
                    throw BizException.of(ErrorCode.PARAM_ERROR, "finalScore cannot exceed max score: " + maxScore);
                }
                targetFinalScore = request.getFinalScore();
            }

            if (!targetFinalScore.equals(currentFinalScore)) {
                int maxScore = safeInt(attemptQuestion.getScore());
                int isCorrect = (maxScore > 0 && targetFinalScore >= maxScore) ? 1 : 0;
                answerMapper.updateScoring(answer.getId(), safeInt(answer.getAutoScore()), targetFinalScore, isCorrect, now);

                QbGradingRecord record = new QbGradingRecord();
                record.setAnswerId(answer.getId());
                record.setGradingMode(GradingModeEnum.MANUAL.getCode());
                record.setScore(targetFinalScore);
                record.setDetailJson("{\"source\":\"appeal\"}");
                record.setNeedsReview(0);
                record.setReviewerId(handlerId);
                record.setReviewComment(request.getDecisionComment());
                record.setIsFinal(1);
                gradingRecordMapper.insert(record);

                updateAttemptScoreByDelta(answer.getAttemptId(), attemptQuestion.getQuestionType(), targetFinalScore - currentFinalScore);
                userAbilityService.recomputeAndPersist(answer.getUserId());
                targetAppealStatus = AppealStatusEnum.RESOLVED.getCode();
            } else {
                targetAppealStatus = AppealStatusEnum.APPROVED.getCode();
            }
        }

        appealMapper.updateHandle(
                appealId,
                targetAppealStatus,
                handlerId,
                now,
                request.getDecisionComment(),
                targetFinalScore
        );
        refreshAttemptReviewState(answer.getAttemptId());
    }

    private void updateAttemptScoreByDelta(Long attemptId, Integer questionType, int delta) {
        if (attemptId == null || delta == 0) {
            return;
        }
        int deltaObjective = isObjective(questionType) ? delta : 0;
        int deltaSubjective = isObjective(questionType) ? 0 : delta;
        attemptMapper.updateScoreDelta(attemptId, delta, deltaObjective, deltaSubjective);
    }

    private boolean isObjective(Integer questionType) {
        if (questionType == null) {
            return false;
        }
        return questionType == QuestionTypeEnum.SINGLE.getCode()
                || questionType == QuestionTypeEnum.MULTIPLE.getCode()
                || questionType == QuestionTypeEnum.TRUE_FALSE.getCode()
                || questionType == QuestionTypeEnum.BLANK.getCode();
    }

    private Integer normalizeStatus(Integer status, boolean defaultPendingForTeacher) {
        if (status == null) {
            return defaultPendingForTeacher ? AppealStatusEnum.PENDING.getCode() : null;
        }
        if (status < AppealStatusEnum.PENDING.getCode() || status > AppealStatusEnum.RESOLVED.getCode()) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "status must be between 1 and 4");
        }
        return status;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private void refreshAttemptReviewState(Long attemptId) {
        if (attemptId == null) {
            return;
        }
        QbAttempt attempt = attemptMapper.selectById(attemptId);
        if (attempt == null) {
            return;
        }
        long pendingReviewCount = answerMapper.countPendingReviewByAttemptId(attemptId);
        long pendingAppealCount = appealMapper.countPendingByAttemptId(attemptId);
        boolean hasPendingWork = pendingReviewCount > 0 || pendingAppealCount > 0;
        attempt.setNeedsReview(hasPendingWork ? 1 : 0);
        attempt.setStatus(hasPendingWork ? AttemptStatusEnum.GRADING.getCode() : AttemptStatusEnum.GRADED.getCode());
        attemptMapper.updateAfterSubmit(attempt);
    }
}
