package com.xyz.question_bank_management_system.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.common.enums.AttemptStatusEnum;
import com.xyz.question_bank_management_system.entity.QbAnswer;
import com.xyz.question_bank_management_system.entity.QbAssignment;
import com.xyz.question_bank_management_system.entity.QbAttempt;
import com.xyz.question_bank_management_system.entity.QbAttemptQuestion;
import com.xyz.question_bank_management_system.entity.QbGradingRecord;
import com.xyz.question_bank_management_system.entity.QbLlmCall;
import com.xyz.question_bank_management_system.entity.SysUser;
import com.xyz.question_bank_management_system.exception.BizException;
import com.xyz.question_bank_management_system.exception.ErrorCode;
import com.xyz.question_bank_management_system.mapper.QbAnswerMapper;
import com.xyz.question_bank_management_system.mapper.QbAppealMapper;
import com.xyz.question_bank_management_system.mapper.QbAssignmentMapper;
import com.xyz.question_bank_management_system.mapper.QbAttemptMapper;
import com.xyz.question_bank_management_system.mapper.QbAttemptQuestionMapper;
import com.xyz.question_bank_management_system.mapper.QbGradingRecordMapper;
import com.xyz.question_bank_management_system.mapper.QbLlmCallMapper;
import com.xyz.question_bank_management_system.mapper.SysUserMapper;
import com.xyz.question_bank_management_system.service.LlmService;
import com.xyz.question_bank_management_system.service.TeacherReviewService;
import com.xyz.question_bank_management_system.service.UserAbilityService;
import com.xyz.question_bank_management_system.util.PageParamUtil;
import com.xyz.question_bank_management_system.vo.TeacherAnswerEvidenceVO;
import com.xyz.question_bank_management_system.vo.TeacherAssignmentScoreItemVO;
import com.xyz.question_bank_management_system.vo.TeacherReviewAnswerItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherReviewServiceImpl implements TeacherReviewService {

    private final QbAnswerMapper answerMapper;
    private final QbAppealMapper appealMapper;
    private final QbAssignmentMapper assignmentMapper;
    private final QbAttemptMapper attemptMapper;
    private final QbAttemptQuestionMapper attemptQuestionMapper;
    private final QbGradingRecordMapper gradingRecordMapper;
    private final QbLlmCallMapper llmCallMapper;
    private final SysUserMapper sysUserMapper;
    private final LlmService llmService;
    private final UserAbilityService userAbilityService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PageResponse<TeacherReviewAnswerItemVO> reviewAnswers(Long assignmentId, Boolean needsReview, long page, long size) {
        long safePage = PageParamUtil.normalizePage(page);
        long safeSize = PageParamUtil.normalizeSize(size);
        long offset = PageParamUtil.offset(safePage, safeSize);
        Integer needsReviewInt = needsReview == null ? 1 : (needsReview ? 1 : 0);

        List<TeacherReviewAnswerItemVO> rows = answerMapper.pageTeacherReview(assignmentId, needsReviewInt, offset, safeSize);
        long total = answerMapper.countTeacherReview(assignmentId, needsReviewInt);
        return PageResponse.of(safePage, safeSize, total, rows);
    }

    @Override
    public TeacherAnswerEvidenceVO evidence(Long answerId) {
        QbAnswer answer = answerMapper.selectById(answerId);
        if (answer == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "answer not found");
        }
        QbAttemptQuestion aq = attemptQuestionMapper.selectById(answer.getAttemptQuestionId());
        if (aq == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "attempt question not found");
        }

        TeacherAnswerEvidenceVO vo = new TeacherAnswerEvidenceVO();
        vo.setAnswerId(answer.getId());
        vo.setStudentAnswer(answer.getAnswerContent());

        TeacherAnswerEvidenceVO.StudentVO student = new TeacherAnswerEvidenceVO.StudentVO();
        student.setId(answer.getUserId());
        SysUser user = sysUserMapper.selectById(answer.getUserId());
        student.setDisplayName(user == null ? null : user.getDisplayName());
        vo.setStudent(student);

        vo.setQuestionSnapshot(parseSnapshot(aq.getSnapshotJson()));

        List<QbGradingRecord> records = gradingRecordMapper.selectByAnswerId(answerId);
        List<TeacherAnswerEvidenceVO.GradingRecordVO> recordVos = new ArrayList<>();
        for (QbGradingRecord r : records) {
            TeacherAnswerEvidenceVO.GradingRecordVO rv = new TeacherAnswerEvidenceVO.GradingRecordVO();
            rv.setGradingMode(r.getGradingMode());
            rv.setScore(r.getScore());
            rv.setConfidence(r.getConfidence());
            rv.setNeedsReview(r.getNeedsReview());
            rv.setDetailJson(r.getDetailJson());
            rv.setReviewComment(r.getReviewComment());

            if (r.getLlmCallId() != null) {
                QbLlmCall call = llmCallMapper.selectById(r.getLlmCallId());
                if (call != null) {
                    TeacherAnswerEvidenceVO.LlmCallVO llm = new TeacherAnswerEvidenceVO.LlmCallVO();
                    llm.setLlmCallId(call.getId());
                    llm.setModelName(call.getModelName());
                    llm.setPromptText(call.getPromptText());
                    llm.setResponseText(call.getResponseText());
                    llm.setResponseJson(call.getResponseJson());
                    llm.setCallStatus(call.getCallStatus());
                    rv.setLlmCall(llm);
                }
            }
            recordVos.add(rv);
        }
        vo.setGradingRecords(recordVos);
        return vo;
    }

    @Override
    @Transactional
    public void manualGrade(Long answerId, Integer score, String comment, Long reviewerId) {
        QbAnswer answer = answerMapper.selectById(answerId);
        if (answer == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "answer not found");
        }

        QbAttemptQuestion aq = attemptQuestionMapper.selectById(answer.getAttemptQuestionId());
        if (aq == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "attempt question not found");
        }
        int maxScore = aq.getScore() == null ? 0 : aq.getScore();
        if (score == null || score < 0 || score > maxScore) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "score must be between 0 and " + maxScore);
        }

        int previousScore = answer.getFinalScore() == null ? 0 : answer.getFinalScore();
        int safeScore = applyScoreAndDelta(answer, aq, score);

        QbGradingRecord record = new QbGradingRecord();
        record.setAnswerId(answerId);
        record.setGradingMode(3);
        record.setScore(safeScore);
        record.setDetailJson("{\"source\":\"manual\"}");
        record.setNeedsReview(0);
        record.setReviewerId(reviewerId);
        record.setReviewComment(comment);
        record.setIsFinal(1);
        gradingRecordMapper.insert(record);

        if (safeScore != previousScore) {
            userAbilityService.recomputeAndPersist(answer.getUserId());
        }
        refreshAttemptReviewState(answer.getAttemptId());
    }

    @Override
    @Transactional
    public List<Long> llmRetry(Long answerId, String modelName, Double temperature, Integer times) {
        QbAnswer answer = answerMapper.selectById(answerId);
        if (answer == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "answer not found");
        }
        QbAttemptQuestion aq = attemptQuestionMapper.selectById(answer.getAttemptQuestionId());
        if (aq == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "attempt question not found");
        }

        int safeTimes = times == null ? 1 : Math.max(1, Math.min(5, times));
        String prompt = buildLlmRetryPrompt(answer, aq, modelName, temperature);

        List<Long> llmCallIds = new ArrayList<>();
        boolean abilityNeedsRefresh = false;
        for (int i = 0; i < safeTimes; i++) {
            QbLlmCall call = llmService.chatCompletion(2, answerId, prompt);
            if (call != null && call.getId() != null) {
                llmCallIds.add(call.getId());
            }
            if (call == null || call.getCallStatus() == null || call.getCallStatus() != 1) {
                continue;
            }
            ParsedLlmGrade parsed = parseLlmGradeResult(call);
            if (parsed == null || parsed.score == null) {
                continue;
            }
            int previousScore = answer.getFinalScore() == null ? 0 : answer.getFinalScore();
            int finalScore = applyScoreAndDelta(answer, aq, parsed.score);
            QbGradingRecord record = new QbGradingRecord();
            record.setAnswerId(answerId);
            record.setGradingMode(2);
            record.setScore(finalScore);
            record.setDetailJson(parsed.detailJson);
            record.setLlmCallId(call.getId());
            record.setConfidence(parsed.confidence);
            record.setNeedsReview(parsed.needsReview ? 1 : 0);
            record.setReviewComment(parsed.comment);
            record.setIsFinal(parsed.needsReview ? 0 : 1);
            gradingRecordMapper.insert(record);
            if (finalScore != previousScore) {
                abilityNeedsRefresh = true;
            }
        }
        if (abilityNeedsRefresh) {
            userAbilityService.recomputeAndPersist(answer.getUserId());
        }
        refreshAttemptReviewState(answer.getAttemptId());
        return llmCallIds;
    }

    @Override
    public PageResponse<TeacherAssignmentScoreItemVO> assignmentScores(Long assignmentId, long page, long size) {
        QbAssignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "assignment not found");
        }

        long safePage = PageParamUtil.normalizePage(page);
        long safeSize = PageParamUtil.normalizeSize(size);
        long offset = PageParamUtil.offset(safePage, safeSize);

        List<TeacherAssignmentScoreItemVO> rows = attemptMapper.pageByAssignmentForTeacher(assignmentId, offset, safeSize);
        long total = attemptMapper.countByAssignmentForTeacher(assignmentId);
        return PageResponse.of(safePage, safeSize, total, rows);
    }

    private String buildLlmRetryPrompt(QbAnswer answer, QbAttemptQuestion aq, String modelName, Double temperature) {
        String questionTitle = null;
        String stem = null;
        String standardAnswer = null;
        String analysisText = null;
        try {
            JsonNode root = objectMapper.readTree(aq.getSnapshotJson());
            questionTitle = root.path("title").asText(null);
            stem = root.path("stem").asText(null);
            standardAnswer = root.path("standardAnswer").asText(null);
            analysisText = root.path("analysisText").asText(null);
        } catch (Exception ignore) {
        }
        int maxScore = aq.getScore() == null ? 0 : aq.getScore();
        String expectedModel = modelName == null ? "" : modelName.trim();
        String expectedTemperature = temperature == null ? "" : String.valueOf(temperature);

        return "You are a strict grading assistant. Return JSON only: {\"score\":number,\"confidence\":number,\"needsReview\":boolean,\"comment\":string}.\n"
                + "maxScore=" + maxScore + "\n"
                + "preferredModel=" + expectedModel + "\n"
                + "preferredTemperature=" + expectedTemperature + "\n"
                + "questionTitle=" + nullToEmpty(questionTitle) + "\n"
                + "stem=" + nullToEmpty(stem) + "\n"
                + "standardAnswer=" + nullToEmpty(standardAnswer) + "\n"
                + "analysisText=" + nullToEmpty(analysisText) + "\n"
                + "studentAnswer=" + nullToEmpty(answer.getAnswerContent()) + "\n";
    }

    private int applyScoreAndDelta(QbAnswer answer, QbAttemptQuestion aq, int score) {
        int maxScore = aq.getScore() == null ? 0 : aq.getScore();
        int safeScore = Math.max(0, Math.min(maxScore, score));
        int oldFinalScore = answer.getFinalScore() == null ? 0 : answer.getFinalScore();
        int autoScore = answer.getAutoScore() == null ? 0 : answer.getAutoScore();
        int isCorrect = safeScore >= maxScore && maxScore > 0 ? 1 : 0;

        answerMapper.updateScoring(answer.getId(), autoScore, safeScore, isCorrect, LocalDateTime.now());

        int delta = safeScore - oldFinalScore;
        if (delta != 0) {
            if (isObjectiveQuestionType(aq.getQuestionType())) {
                attemptMapper.updateScoreDelta(answer.getAttemptId(), delta, delta, 0);
            } else {
                attemptMapper.updateScoreDelta(answer.getAttemptId(), delta, 0, delta);
            }
        }
        answer.setFinalScore(safeScore);
        return safeScore;
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

    private ParsedLlmGrade parseLlmGradeResult(QbLlmCall call) {
        try {
            String content = llmService.extractContent(call.getResponseText());
            if (content == null || content.isBlank()) {
                return null;
            }
            JsonNode json = parseJsonNode(content);
            if (json == null || !json.isObject() || !json.has("score")) {
                return null;
            }
            ParsedLlmGrade result = new ParsedLlmGrade();
            result.score = json.get("score").asInt();
            if (json.has("confidence") && !json.get("confidence").isNull()) {
                result.confidence = json.get("confidence").asDouble();
            }
            result.needsReview = !json.has("needsReview") || json.get("needsReview").asBoolean(true);
            if (result.confidence != null && result.confidence < 0.55) {
                result.needsReview = true;
            }
            if (json.has("comment") && !json.get("comment").isNull()) {
                result.comment = json.get("comment").asText();
            }
            result.detailJson = json.toString();
            return result;
        } catch (Exception ignore) {
            return null;
        }
    }

    private JsonNode parseJsonNode(String content) {
        try {
            return objectMapper.readTree(content);
        } catch (Exception ignore) {
            String normalized = content.trim();
            if (normalized.startsWith("```")) {
                normalized = normalized.replaceFirst("^```(?:json)?\\s*", "");
                normalized = normalized.replaceFirst("\\s*```$", "");
                try {
                    return objectMapper.readTree(normalized.trim());
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
    }

    private boolean isObjectiveQuestionType(Integer questionType) {
        if (questionType == null) {
            return false;
        }
        return questionType == 1 || questionType == 2 || questionType == 3 || questionType == 4;
    }

    private Object parseSnapshot(String snapshotJson) {
        if (snapshotJson == null || snapshotJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(snapshotJson, Object.class);
        } catch (Exception ignore) {
            return snapshotJson;
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static class ParsedLlmGrade {
        Integer score;
        Double confidence;
        boolean needsReview = true;
        String comment;
        String detailJson;
    }
}
