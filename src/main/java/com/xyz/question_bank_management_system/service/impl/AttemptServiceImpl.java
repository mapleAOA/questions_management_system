package com.xyz.question_bank_management_system.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.common.enums.*;
import com.xyz.question_bank_management_system.dto.PracticeStartRequest;
import com.xyz.question_bank_management_system.dto.SaveAnswerDraftRequest;
import com.xyz.question_bank_management_system.entity.*;
import com.xyz.question_bank_management_system.exception.BizException;
import com.xyz.question_bank_management_system.exception.ErrorCode;
import com.xyz.question_bank_management_system.mapper.*;
import com.xyz.question_bank_management_system.service.AttemptService;
import com.xyz.question_bank_management_system.service.LlmService;
import com.xyz.question_bank_management_system.util.HashUtil;
import com.xyz.question_bank_management_system.util.PageParamUtil;
import com.xyz.question_bank_management_system.util.TextRepairUtil;
import com.xyz.question_bank_management_system.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AttemptServiceImpl implements AttemptService {

    private final QbAssignmentMapper assignmentMapper;
    private final QbAssignmentTargetMapper targetMapper;

    private final QbAttemptMapper attemptMapper;
    private final QbAttemptQuestionMapper attemptQuestionMapper;
    private final QbAnswerMapper answerMapper;
    private final QbGradingRecordMapper gradingRecordMapper;

    private final QbPaperQuestionMapper paperQuestionMapper;
    private final QbQuestionMapper questionMapper;
    private final QbQuestionOptionMapper optionMapper;
    private final QbQuestionCaseMapper caseMapper;
    private final QbQuestionTagMapper questionTagMapper;
    private final QbClassMemberMapper classMemberMapper;

    private final QbQuestionUserStatMapper questionUserStatMapper;
    private final QbWrongQuestionMapper wrongQuestionMapper;
    private final QbTagMasteryMapper tagMasteryMapper;
    private final QbUserAbilityMapper userAbilityMapper;

    private final LlmService llmService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public AttemptStartVO startAssignmentAttempt(Long assignmentId, Long userId) {
        QbAssignment a = assignmentMapper.selectById(assignmentId);
        if (a == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "作业不存在");
        }
        if (a.getPublishStatus() == null || a.getPublishStatus() != AssignmentPublishStatusEnum.PUBLISHED.getCode()) {
            throw BizException.of(ErrorCode.FORBIDDEN, "作业未发布");
        }
        LocalDateTime now = LocalDateTime.now();
        if (a.getStartTime() != null && now.isBefore(a.getStartTime())) {
            throw BizException.of(ErrorCode.FORBIDDEN, "作业未开始");
        }
        if (a.getEndTime() != null && now.isAfter(a.getEndTime())) {
            throw BizException.of(ErrorCode.FORBIDDEN, "作业已结束");
        }

        long targetCount = targetMapper.countByAssignmentId(assignmentId);
        if (targetCount > 0) {
            long me = targetMapper.countByAssignmentAndUser(assignmentId, userId);
            if (me <= 0) {
                throw BizException.of(ErrorCode.FORBIDDEN, "你不在该作业的目标名单中");
            }
        }

        long usedAttempts = attemptMapper.countByAssignmentAndUser(assignmentId, userId);
        int maxAttempts = a.getMaxAttempts() == null ? 1 : a.getMaxAttempts();
        if (maxAttempts > 0 && usedAttempts >= maxAttempts) {
            throw BizException.of(ErrorCode.FORBIDDEN, "已达到最大作答次数");
        }

        int attemptNo = (int) usedAttempts + 1;

        QbAttempt attempt = new QbAttempt();
        attempt.setAssignmentId(assignmentId);
        attempt.setPaperId(a.getPaperId());
        attempt.setUserId(userId);
        attempt.setAttemptType(AttemptTypeEnum.ASSIGNMENT.getCode());
        attempt.setAttemptNo(attemptNo);
        attempt.setStatus(AttemptStatusEnum.IN_PROGRESS.getCode());
        attemptMapper.insert(attempt);

        // 生成 attempt_question 快照
        List<QbPaperQuestion> pqs = paperQuestionMapper.selectByPaperId(a.getPaperId());
        if (pqs == null || pqs.isEmpty()) {
            throw BizException.of(ErrorCode.BIZ_ERROR, "试卷未配置题目");
        }

        List<QbPaperQuestion> ordered = new ArrayList<>(pqs);
        if (a.getShuffleQuestions() != null && a.getShuffleQuestions() == 1) {
            Collections.shuffle(ordered);
        }

        List<QbAttemptQuestion> aqList = new ArrayList<>();
        int order = 1;
        for (QbPaperQuestion pq : ordered) {
            String snapshotJson = repairSnapshotMojibake(pq.getSnapshotJson());
            if (a.getShuffleOptions() != null && a.getShuffleOptions() == 1) {
                snapshotJson = shuffleOptionsInSnapshot(snapshotJson);
            }
            String snapshotHash = HashUtil.sha256(snapshotJson);

            Map<String, Object> snap = readSnapshotMap(snapshotJson);
            Integer qt = asInt(snap.get("questionType"));
            Integer diff = asInt(snap.get("difficulty"));
            Object tagIdsObj = snap.get("tagIds");
            String tagIdsJson = null;
            try {
                if (tagIdsObj != null) {
                    tagIdsJson = objectMapper.writeValueAsString(tagIdsObj);
                }
            } catch (Exception ignore) {}

            QbAttemptQuestion aq = new QbAttemptQuestion();
            aq.setAttemptId(attempt.getId());
            aq.setQuestionId(pq.getQuestionId());
            aq.setOrderNo(order);
            aq.setScore(pq.getScore());
            aq.setSnapshotJson(snapshotJson);
            aq.setSnapshotHash(snapshotHash);
            aq.setQuestionType(qt);
            aq.setDifficulty(diff);
            aq.setTagIdsJson(tagIdsJson);
            aqList.add(aq);
            order++;
        }
        attemptQuestionMapper.batchInsert(aqList);
        initAnswersForAttempt(attempt.getId(), userId);

        return new AttemptStartVO(attempt.getId(), attemptNo, assignmentId, a.getPaperId(), attempt.getStatus());
    }

    @Override
    @Transactional
    public AttemptStartVO startPracticeAttempt(PracticeStartRequest request, Long userId) {
        normalizePracticeMode(request.getMode());
        int totalScore = normalizePracticeTotalScore(request.getTotalScore());

        List<Long> tagIds = normalizeLongList(request.getScope() == null ? null : request.getScope().getTagIds());
        List<String> chapters = normalizeStringList(request.getScope() == null ? null : request.getScope().getChapters());
        List<Integer> questionTypes = normalizeQuestionTypes(request.getScope() == null ? null : request.getScope().getQuestionTypes());
        List<Long> questionIds = normalizeLongList(request.getScope() == null ? null : request.getScope().getQuestionIds());
        List<Long> visibleTeacherIds = resolveVisibleTeacherIds(userId);

        List<QbQuestion> selected;
        if (questionIds != null && !questionIds.isEmpty()) {
            selected = selectPracticeQuestionsByIds(questionIds, visibleTeacherIds);
        } else {
            int questionCount = Math.max(1, Math.min(50, totalScore / 10));
            long candidateLimit = Math.max(questionCount * 5L, 50L);
            List<QbQuestion> candidates = questionMapper.searchForPractice(tagIds, chapters, questionTypes, visibleTeacherIds, candidateLimit);
            if (candidates == null || candidates.isEmpty()) {
                throw BizException.of(ErrorCode.BIZ_ERROR, "no published questions match current scope");
            }
            Collections.shuffle(candidates);
            int picked = Math.min(questionCount, candidates.size());
            selected = candidates.subList(0, picked);
        }
        int[] scores = splitScores(totalScore, selected.size());

        long usedAttempts = attemptMapper.countByUser(userId, AttemptTypeEnum.PRACTICE.getCode());
        int attemptNo = (int) usedAttempts + 1;
        QbAttempt attempt = new QbAttempt();
        attempt.setAssignmentId(null);
        attempt.setPaperId(null);
        attempt.setUserId(userId);
        attempt.setAttemptType(AttemptTypeEnum.PRACTICE.getCode());
        attempt.setAttemptNo(attemptNo);
        attempt.setStatus(AttemptStatusEnum.IN_PROGRESS.getCode());
        attemptMapper.insert(attempt);

        List<QbAttemptQuestion> aqList = new ArrayList<>();
        int orderNo = 1;
        for (QbQuestion q : selected) {
            String snapshotJson = repairSnapshotMojibake(buildQuestionSnapshot(q.getId()));
            String snapshotHash = HashUtil.sha256(snapshotJson);
            List<Long> qTagIds = questionTagMapper.selectTagIdsByQuestionId(q.getId());
            String tagIdsJson;
            try {
                tagIdsJson = objectMapper.writeValueAsString(qTagIds);
            } catch (Exception e) {
                tagIdsJson = "[]";
            }

            QbAttemptQuestion aq = new QbAttemptQuestion();
            aq.setAttemptId(attempt.getId());
            aq.setQuestionId(q.getId());
            aq.setOrderNo(orderNo);
            aq.setScore(scores[orderNo - 1]);
            aq.setSnapshotJson(snapshotJson);
            aq.setSnapshotHash(snapshotHash);
            aq.setQuestionType(q.getQuestionType());
            aq.setDifficulty(q.getDifficulty());
            aq.setTagIdsJson(tagIdsJson);
            aqList.add(aq);
            orderNo++;
        }
        attemptQuestionMapper.batchInsert(aqList);
        initAnswersForAttempt(attempt.getId(), userId);

        return new AttemptStartVO(attempt.getId(), attemptNo, null, null, attempt.getStatus());
    }

    @Override
    public List<AttemptQuestionVO> getAttemptQuestions(Long attemptId, Long userId) {
        QbAttempt attempt = attemptMapper.selectById(attemptId);
        if (attempt == null) throw BizException.of(ErrorCode.NOT_FOUND, "作答不存在");
        if (!Objects.equals(attempt.getUserId(), userId)) {
            throw BizException.of(ErrorCode.FORBIDDEN, "无权访问该作答");
        }

        List<QbAttemptQuestion> aqs = attemptQuestionMapper.selectByAttemptId(attemptId);
        List<QbAnswer> answers = answerMapper.selectByAttemptId(attemptId);
        Map<Long, QbAnswer> byAttemptQuestionId = new HashMap<>();
        for (QbAnswer a : answers) {
            byAttemptQuestionId.put(a.getAttemptQuestionId(), a);
        }

        List<AttemptQuestionVO> res = new ArrayList<>();
        for (QbAttemptQuestion aq : aqs) {
            AttemptQuestionVO vo = new AttemptQuestionVO();
            vo.setAttemptQuestionId(aq.getId());
            vo.setQuestionId(aq.getQuestionId());
            vo.setOrderNo(aq.getOrderNo());
            vo.setScore(aq.getScore());
            vo.setSnapshotJson(sanitizeSnapshotForStudent(repairSnapshotMojibake(aq.getSnapshotJson())));

            QbAnswer ans = byAttemptQuestionId.get(aq.getId());
            if (ans != null) {
                vo.setAnswerId(ans.getId());
                vo.setAnswerContent(ans.getAnswerContent());
                vo.setAnswerStatus(ans.getAnswerStatus());
            }
            res.add(vo);
        }
        return res;
    }

    @Override
    public void saveDraft(Long answerId, Long userId, SaveAnswerDraftRequest request) {
        QbAnswer ans = answerMapper.selectById(answerId);
        if (ans == null) throw BizException.of(ErrorCode.NOT_FOUND, "答案不存在");
        if (!Objects.equals(ans.getUserId(), userId)) throw BizException.of(ErrorCode.FORBIDDEN, "无权修改该答案");
        QbAttempt attempt = attemptMapper.selectById(ans.getAttemptId());
        if (attempt == null) throw BizException.of(ErrorCode.NOT_FOUND, "作答不存在");
        if (!Objects.equals(attempt.getUserId(), userId)) throw BizException.of(ErrorCode.FORBIDDEN, "无权访问该作答");
        if (attempt.getStatus() == null || attempt.getStatus() != AttemptStatusEnum.IN_PROGRESS.getCode()) {
            throw BizException.of(ErrorCode.FORBIDDEN, "当前状态不允许保存草稿");
        }
        answerMapper.updateDraft(answerId, normalizeAnswerContent(request.getAnswerContent()));
    }

    @Override
    public void submitAnswer(Long answerId, Long userId, SaveAnswerDraftRequest request) {
        QbAnswer ans = answerMapper.selectById(answerId);
        if (ans == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "answer not found");
        }
        if (!Objects.equals(ans.getUserId(), userId)) {
            throw BizException.of(ErrorCode.FORBIDDEN, "no permission to submit this answer");
        }
        QbAttempt attempt = attemptMapper.selectById(ans.getAttemptId());
        if (attempt == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "attempt not found");
        }
        if (!Objects.equals(attempt.getUserId(), userId)) {
            throw BizException.of(ErrorCode.FORBIDDEN, "no permission to access this attempt");
        }
        if (attempt.getStatus() == null || attempt.getStatus() != AttemptStatusEnum.IN_PROGRESS.getCode()) {
            throw BizException.of(ErrorCode.FORBIDDEN, "attempt is not in progress");
        }
        answerMapper.submitOne(answerId, normalizeAnswerContent(request.getAnswerContent()), LocalDateTime.now());
    }

    @Override
    @Transactional
    public void submitAttempt(Long attemptId, Long userId) {
        QbAttempt attempt = attemptMapper.selectById(attemptId);
        if (attempt == null) throw BizException.of(ErrorCode.NOT_FOUND, "作答不存在");
        if (!Objects.equals(attempt.getUserId(), userId)) throw BizException.of(ErrorCode.FORBIDDEN, "无权提交该作答");
        if (attempt.getStatus() == null || attempt.getStatus() != AttemptStatusEnum.IN_PROGRESS.getCode()) {
            throw BizException.of(ErrorCode.FORBIDDEN, "当前状态不允许提交");
        }

        LocalDateTime now = LocalDateTime.now();
        answerMapper.submitAllByAttempt(attemptId, now);

        List<QbAttemptQuestion> aqs = attemptQuestionMapper.selectByAttemptId(attemptId);
        List<QbAnswer> answers = answerMapper.selectByAttemptId(attemptId);
        Map<Long, QbAnswer> byAttemptQuestionId = new HashMap<>();
        for (QbAnswer a : answers) {
            byAttemptQuestionId.put(a.getAttemptQuestionId(), a);
        }

        int totalPossible = 0;
        int objectiveScore = 0;
        int subjectiveScore = 0;
        int needsReview = 0;

        for (QbAttemptQuestion aq : aqs) {
            totalPossible += (aq.getScore() == null ? 0 : aq.getScore());
            QbAnswer ans = byAttemptQuestionId.get(aq.getId());
            if (ans == null) continue;

            Integer questionType = aq.getQuestionType();
            if (questionType == null) {
                questionType = extractQuestionType(aq.getSnapshotJson());
            }

            // objective
            if (isObjective(questionType)) {
                String correct = extractStandardAnswer(aq.getSnapshotJson());
                boolean ok = isObjectiveCorrect(questionType, correct, ans.getAnswerContent());
                int s = ok ? (aq.getScore() == null ? 0 : aq.getScore()) : 0;

                answerMapper.updateScoring(ans.getId(), s, s, ok ? 1 : 0, now);
                objectiveScore += s;

                QbGradingRecord gr = new QbGradingRecord();
                gr.setAnswerId(ans.getId());
                gr.setGradingMode(GradingModeEnum.AUTO.getCode());
                gr.setScore(s);
                gr.setDetailJson("{\"correct\":\"" + safeJson(correct) + "\",\"answer\":\"" + safeJson(ans.getAnswerContent()) + "\"}");
                gr.setLlmCallId(null);
                gr.setConfidence(1.0);
                gr.setNeedsReview(0);
                gr.setReviewerId(null);
                gr.setReviewComment(null);
                gr.setIsFinal(1);
                gradingRecordMapper.insert(gr);

                // stats
                updateStats(userId, aq, ok ? 1 : 0, now);
                if (!ok) {
                    wrongQuestionMapper.upsertWrong(userId, aq.getQuestionId(), now);
                }
            } else {
                // subjective
                int maxScore = aq.getScore() == null ? 0 : aq.getScore();
                LlmGradeResult llm = tryLlmGrade(ans, aq, maxScore);

                if (llm != null && llm.success && llm.score != null) {
                    int score = Math.max(0, Math.min(maxScore, llm.score));
                    subjectiveScore += score;
                    needsReview = needsReview | (llm.needsReview ? 1 : 0);

                    answerMapper.updateScoring(ans.getId(), 0, score, score == maxScore ? 1 : 0, now);

                    QbGradingRecord gr = new QbGradingRecord();
                    gr.setAnswerId(ans.getId());
                    gr.setGradingMode(GradingModeEnum.LLM.getCode());
                    gr.setScore(score);
                    gr.setDetailJson(llm.rawDetailJson);
                    gr.setLlmCallId(llm.llmCallId);
                    gr.setConfidence(llm.confidence);
                    gr.setNeedsReview(llm.needsReview ? 1 : 0);
                    gr.setReviewerId(null);
                    gr.setReviewComment(llm.comment);
                    gr.setIsFinal(llm.needsReview ? 0 : 1);
                    gradingRecordMapper.insert(gr);

                    updateStats(userId, aq, score == maxScore ? 1 : 0, now);
                    if (score < maxScore) {
                        wrongQuestionMapper.upsertWrong(userId, aq.getQuestionId(), now);
                    }
                } else {
                    // 没有LLM或解析失败：进入人工复核
                    needsReview = 1;
                    answerMapper.updateScoring(ans.getId(), 0, 0, 0, now);
                    updateStats(userId, aq, 0, now);
                    wrongQuestionMapper.upsertWrong(userId, aq.getQuestionId(), now);

                    QbGradingRecord gr = new QbGradingRecord();
                    gr.setAnswerId(ans.getId());
                    gr.setGradingMode(GradingModeEnum.MANUAL.getCode());
                    gr.setScore(0);
                    gr.setDetailJson("{\"msg\":\"need manual review\"}");
                    gr.setNeedsReview(1);
                    gr.setIsFinal(0);
                    gradingRecordMapper.insert(gr);
                }
            }
        }

        int totalScore = objectiveScore + subjectiveScore;
        // duration
        QbAttempt fresh = attemptMapper.selectById(attemptId);
        int durationSec = 0;
        if (fresh != null && fresh.getStartedAt() != null) {
            durationSec = (int) Duration.between(fresh.getStartedAt(), now).getSeconds();
        }

        QbAttempt upd = new QbAttempt();
        upd.setId(attemptId);
        upd.setStatus(needsReview == 1 ? AttemptStatusEnum.GRADING.getCode() : AttemptStatusEnum.GRADED.getCode());
        upd.setSubmittedAt(now);
        upd.setDurationSec(durationSec);
        upd.setTotalScore(totalScore);
        upd.setObjectiveScore(objectiveScore);
        upd.setSubjectiveScore(subjectiveScore);
        upd.setNeedsReview(needsReview);
        attemptMapper.updateAfterSubmit(upd);

        // ability score (very simple): 0~100
        int ability = totalPossible <= 0 ? 0 : (int) Math.round(100.0 * totalScore / totalPossible);
        ability = Math.max(0, Math.min(100, ability));
        userAbilityMapper.upsert(userId, ability);
    }

    @Override
    public AttemptResultVO result(Long attemptId, Long userId) {
        QbAttempt attempt = attemptMapper.selectById(attemptId);
        if (attempt == null) throw BizException.of(ErrorCode.NOT_FOUND, "作答不存在");
        if (!Objects.equals(attempt.getUserId(), userId)) throw BizException.of(ErrorCode.FORBIDDEN, "无权访问");

        AttemptResultVO vo = new AttemptResultVO();
        vo.setAttemptId(attempt.getId());
        vo.setStatus(attempt.getStatus());
        vo.setTotalScore(attempt.getTotalScore());
        vo.setObjectiveScore(attempt.getObjectiveScore());
        vo.setSubjectiveScore(attempt.getSubjectiveScore());
        vo.setNeedsReview(attempt.getNeedsReview());
        vo.setStartedAt(attempt.getStartedAt());
        vo.setSubmittedAt(attempt.getSubmittedAt());
        vo.setDurationSec(attempt.getDurationSec());

        List<QbAnswer> answers = answerMapper.selectByAttemptId(attemptId);
        List<AttemptResultVO.AnswerResultVO> list = new ArrayList<>();
        for (QbAnswer a : answers) {
            AttemptResultVO.AnswerResultVO ar = new AttemptResultVO.AnswerResultVO();
            ar.setAnswerId(a.getId());
            ar.setQuestionId(a.getQuestionId());
            ar.setFinalScore(a.getFinalScore());
            ar.setAutoScore(a.getAutoScore());
            ar.setIsCorrect(a.getIsCorrect());
            ar.setAnswerContent(a.getAnswerContent());
            list.add(ar);
        }
        vo.setAnswers(list);
        return vo;
    }

    @Override
    public PageResponse<QbAttempt> myAttempts(Integer attemptType, long page, long size, Long userId) {
        Integer safeAttemptType = null;
        if (attemptType != null) {
            if (attemptType != AttemptTypeEnum.ASSIGNMENT.getCode()
                    && attemptType != AttemptTypeEnum.PRACTICE.getCode()) {
                throw BizException.of(ErrorCode.PARAM_ERROR, "attemptType must be 1 or 2");
            }
            safeAttemptType = attemptType;
        }
        long safePage = PageParamUtil.normalizePage(page);
        long safeSize = PageParamUtil.normalizeSize(size);
        long offset = PageParamUtil.offset(safePage, safeSize);

        List<QbAttempt> rows = attemptMapper.pageByUser(userId, safeAttemptType, offset, safeSize);
        long total = attemptMapper.countByUser(userId, safeAttemptType);
        return PageResponse.of(safePage, safeSize, total, rows);
    }

    // ================= helpers =================

    private void initAnswersForAttempt(Long attemptId, Long userId) {
        List<QbAttemptQuestion> inserted = attemptQuestionMapper.selectByAttemptId(attemptId);
        for (QbAttemptQuestion aq : inserted) {
            QbAnswer ans = new QbAnswer();
            ans.setAttemptId(attemptId);
            ans.setAttemptQuestionId(aq.getId());
            ans.setQuestionId(aq.getQuestionId());
            ans.setUserId(userId);
            ans.setAnswerContent(null);
            Integer answerFormat = extractAnswerFormat(aq.getSnapshotJson());
            ans.setAnswerFormat(answerFormat == null ? 1 : answerFormat);
            ans.setAnswerStatus(AnswerStatusEnum.DRAFT.getCode());
            ans.setAutoScore(0);
            ans.setFinalScore(0);
            ans.setIsCorrect(0);
            answerMapper.insert(ans);
        }
    }

    private String buildQuestionSnapshot(Long questionId) {
        try {
            QbQuestion q = questionMapper.selectById(questionId);
            if (q == null) {
                throw BizException.of(ErrorCode.NOT_FOUND, "question not found: " + questionId);
            }

            List<QbQuestionOption> options = optionMapper.selectByQuestionId(questionId);
            List<QbQuestionCase> cases = caseMapper.selectByQuestionId(questionId);
            List<Long> tagIds = questionTagMapper.selectTagIdsByQuestionId(questionId);

            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("id", q.getId());
            snapshot.put("title", q.getTitle());
            snapshot.put("questionType", q.getQuestionType());
            snapshot.put("difficulty", q.getDifficulty());
            snapshot.put("chapter", q.getChapter());
            snapshot.put("stem", q.getStem());
            snapshot.put("standardAnswer", q.getStandardAnswer());
            snapshot.put("answerFormat", q.getAnswerFormat());
            snapshot.put("analysisText", q.getAnalysisText());
            snapshot.put("analysisSource", q.getAnalysisSource());
            snapshot.put("tagIds", tagIds);
            snapshot.put("options", options);
            snapshot.put("cases", cases);
            return objectMapper.writeValueAsString(snapshot);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw BizException.of(ErrorCode.BIZ_ERROR, "failed to build question snapshot: " + e.getMessage());
        }
    }

    private String normalizePracticeMode(String mode) {
        if (mode == null || mode.isBlank()) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "mode cannot be blank");
        }
        String normalized = mode.trim().toLowerCase();
        if ("random".equals(normalized) || "adaptive".equals(normalized)) {
            return normalized;
        }
        throw BizException.of(ErrorCode.PARAM_ERROR, "mode must be random or adaptive");
    }

    private int normalizePracticeTotalScore(Integer totalScore) {
        if (totalScore == null) {
            return 100;
        }
        if (totalScore <= 0) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "totalScore must be greater than 0");
        }
        return Math.min(totalScore, 1000);
    }

    private List<Long> normalizeLongList(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<Long> resolveVisibleTeacherIds(Long studentId) {
        List<Long> teacherIds = classMemberMapper.listTeacherIdsByStudentId(studentId);
        if (teacherIds == null || teacherIds.isEmpty()) {
            return List.of();
        }
        return teacherIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<QbQuestion> selectPracticeQuestionsByIds(List<Long> questionIds, List<Long> visibleTeacherIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "questionIds cannot be empty");
        }
        if (questionIds.size() > 100) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "questionIds size cannot exceed 100");
        }
        List<QbQuestion> rows = questionMapper.selectPublishedByIds(questionIds, visibleTeacherIds);
        Map<Long, QbQuestion> byId = new HashMap<>();
        for (QbQuestion row : rows) {
            byId.put(row.getId(), row);
        }
        List<QbQuestion> ordered = new ArrayList<>();
        for (Long qid : questionIds) {
            QbQuestion q = byId.get(qid);
            if (q == null) {
                throw BizException.of(ErrorCode.PARAM_ERROR, "question is not published or not found: " + qid);
            }
            ordered.add(q);
        }
        return ordered;
    }

    private List<String> normalizeStringList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<String> normalized = values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
        return normalized.isEmpty() ? null : normalized;
    }

    private List<Integer> normalizeQuestionTypes(List<Integer> questionTypes) {
        if (questionTypes == null || questionTypes.isEmpty()) {
            return null;
        }
        List<Integer> normalized = questionTypes.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        for (Integer t : normalized) {
            QuestionTypeEnum type = QuestionTypeEnum.of(t);
            if (type == null || !type.isEnabledNow()) {
                throw BizException.of(ErrorCode.PARAM_ERROR, "questionType is disabled or invalid: " + t);
            }
        }
        return normalized;
    }

    private int[] splitScores(int totalScore, int questionCount) {
        int[] scores = new int[questionCount];
        int base = totalScore / questionCount;
        int rem = totalScore % questionCount;
        for (int i = 0; i < questionCount; i++) {
            scores[i] = base + (i < rem ? 1 : 0);
        }
        return scores;
    }

    private boolean isObjective(Integer questionType) {
        if (questionType == null) return false;
        return questionType == QuestionTypeEnum.SINGLE.getCode()
                || questionType == QuestionTypeEnum.MULTIPLE.getCode()
                || questionType == QuestionTypeEnum.TRUE_FALSE.getCode()
                || questionType == QuestionTypeEnum.BLANK.getCode();
    }

    private Integer extractAnswerFormat(String snapshotJson) {
        try {
            Map<String, Object> m = readSnapshotMap(snapshotJson);
            return asInt(m.get("answerFormat"));
        } catch (Exception e) {
            return null;
        }
    }

    private Integer extractQuestionType(String snapshotJson) {
        try {
            Map<String, Object> m = readSnapshotMap(snapshotJson);
            return asInt(m.get("questionType"));
        } catch (Exception e) {
            return null;
        }
    }

    private String extractStandardAnswer(String snapshotJson) {
        try {
            Map<String, Object> m = readSnapshotMap(snapshotJson);
            Object v = m.get("standardAnswer");
            return v == null ? null : String.valueOf(v);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isObjectiveCorrect(Integer questionType, String standardAnswer, String userAnswer) {
        if (standardAnswer == null) standardAnswer = "";
        if (userAnswer == null) userAnswer = "";
        String sa = standardAnswer.trim();
        String ua = userAnswer.trim();

        if (questionType == QuestionTypeEnum.SINGLE.getCode()) {
            return sa.equalsIgnoreCase(ua);
        }
        if (questionType == QuestionTypeEnum.MULTIPLE.getCode()) {
            Set<String> s1 = splitMulti(sa);
            Set<String> s2 = splitMulti(ua);
            return s1.equals(s2);
        }
        if (questionType == QuestionTypeEnum.TRUE_FALSE.getCode()) {
            return normalizeTF(sa).equals(normalizeTF(ua));
        }
        if (questionType == QuestionTypeEnum.BLANK.getCode()) {
            // 简化：去空白后比较
            return sa.replaceAll("\\s+", "").equalsIgnoreCase(ua.replaceAll("\\s+", ""));
        }
        return false;
    }

    private Set<String> splitMulti(String s) {
        s = s.trim();
        if (s.isEmpty()) return new HashSet<>();
        if (s.contains(",")) {
            String[] arr = s.split(",");
            Set<String> set = new HashSet<>();
            for (String a : arr) {
                if (!a.isBlank()) set.add(a.trim().toUpperCase());
            }
            return set;
        }
        // e.g. "AC" -> A,C
        Set<String> set = new HashSet<>();
        for (char c : s.toCharArray()) {
            if (!Character.isWhitespace(c)) set.add(String.valueOf(c).toUpperCase());
        }
        return set;
    }

    private String normalizeTF(String s) {
        s = s.trim().toLowerCase();
        if (s.equals("t") || s.equals("true") || s.equals("1") || s.equals("对") || s.equals("正确")) return "true";
        if (s.equals("f") || s.equals("false") || s.equals("0") || s.equals("错") || s.equals("错误")) return "false";
        return s;
    }

    private void updateStats(Long userId, QbAttemptQuestion aq, int correctInc, LocalDateTime at) {
        questionUserStatMapper.upsert(userId, aq.getQuestionId(), correctInc, at);

        List<Long> tagIds = parseTagIds(aq.getTagIdsJson());
        if (tagIds != null) {
            for (Long tid : tagIds) {
                if (tid == null) continue;
                double init = correctInc; // 第一次：0或1
                tagMasteryMapper.upsertAttempt(userId, tid, correctInc, init);
            }
        }
    }

    private List<Long> parseTagIds(String tagIdsJson) {
        if (tagIdsJson == null || tagIdsJson.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(tagIdsJson, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Map<String, Object> readSnapshotMap(String snapshotJson) {
        try {
            return objectMapper.readValue(snapshotJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private Integer asInt(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try {
            return Integer.parseInt(String.valueOf(obj));
        } catch (Exception e) {
            return null;
        }
    }

    private String repairSnapshotMojibake(String snapshotJson) {
        if (snapshotJson == null || snapshotJson.isBlank()) {
            return snapshotJson;
        }
        try {
            JsonNode root = objectMapper.readTree(snapshotJson);
            repairMojibakeInNode(root);
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return snapshotJson;
        }
    }

    private void repairMojibakeInNode(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            var obj = (com.fasterxml.jackson.databind.node.ObjectNode) node;
            var fields = obj.fields();
            while (fields.hasNext()) {
                var entry = fields.next();
                JsonNode child = entry.getValue();
                if (child != null && child.isTextual()) {
                    obj.put(entry.getKey(), TextRepairUtil.repairGbkUtf8Mojibake(child.asText()));
                } else {
                    repairMojibakeInNode(child);
                }
            }
            return;
        }
        if (node.isArray()) {
            var arr = (com.fasterxml.jackson.databind.node.ArrayNode) node;
            for (int i = 0; i < arr.size(); i++) {
                JsonNode child = arr.get(i);
                if (child != null && child.isTextual()) {
                    arr.set(i, objectMapper.getNodeFactory().textNode(TextRepairUtil.repairGbkUtf8Mojibake(child.asText())));
                } else {
                    repairMojibakeInNode(child);
                }
            }
        }
    }

    private String sanitizeSnapshotForStudent(String snapshotJson) {
        try {
            JsonNode root = objectMapper.readTree(snapshotJson);
            if (root.isObject()) {
                ((com.fasterxml.jackson.databind.node.ObjectNode) root).remove(List.of("standardAnswer", "analysisText", "analysisSource", "analysisLlmCallId"));

                // remove isCorrect from options
                JsonNode options = root.get("options");
                if (options != null && options.isArray()) {
                    for (JsonNode o : options) {
                        if (o.isObject()) {
                            ((com.fasterxml.jackson.databind.node.ObjectNode) o).remove("isCorrect");
                        }
                    }
                }
            }
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return snapshotJson;
        }
    }

    private String shuffleOptionsInSnapshot(String snapshotJson) {
        try {
            JsonNode root = objectMapper.readTree(snapshotJson);
            JsonNode options = root.get("options");
            if (options != null && options.isArray() && options.size() > 1) {
                List<JsonNode> list = new ArrayList<>();
                options.forEach(list::add);
                Collections.shuffle(list);
                com.fasterxml.jackson.databind.node.ArrayNode arr = objectMapper.createArrayNode();
                for (JsonNode n : list) {
                    arr.add(n);
                }
                if (root.isObject()) {
                    ((com.fasterxml.jackson.databind.node.ObjectNode) root).set("options", arr);
                }
            }
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return snapshotJson;
        }
    }

    private String safeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String normalizeAnswerContent(String answerContent) {
        return answerContent == null ? "" : answerContent;
    }

    private LlmGradeResult tryLlmGrade(QbAnswer ans, QbAttemptQuestion aq, int maxScore) {
        // 如果没有配置apiKey，LlmServiceImpl会返回 callStatus=2，此处视为失败。
        try {
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
            } catch (Exception ignore) {}

            String prompt = "你是阅卷老师。请根据题目、参考答案/解析，对学生答案评分，满分" + maxScore + "分。\n" +
                    "要求只输出JSON：{\"score\":number,\"confidence\":number,\"needsReview\":boolean,\"comment\":string}\n" +
                    "confidence范围0~1；needsReview在不确定、答案含糊、可能需要人工复核时为true。\n\n" +
                    "题目标题：" + (questionTitle == null ? "" : questionTitle) + "\n" +
                    "题干：" + (stem == null ? "" : stem) + "\n" +
                    "参考答案：" + (standardAnswer == null ? "" : standardAnswer) + "\n" +
                    "解析：" + (analysisText == null ? "" : analysisText) + "\n" +
                    "学生答案：" + (ans.getAnswerContent() == null ? "" : ans.getAnswerContent()) + "\n";

            QbLlmCall call = llmService.chatCompletion(2, ans.getId(), prompt);
            if (call.getCallStatus() == null || call.getCallStatus() != 1) {
                return null;
            }
            String content = llmService.extractContent(call.getResponseText());
            if (content == null) return null;

            JsonNode json;
            try {
                json = objectMapper.readTree(content);
            } catch (Exception e) {
                // 有些模型会在代码块中返回
                content = content.trim();
                content = content.replaceAll("^```json", "").replaceAll("```$", "").trim();
                try {
                    json = objectMapper.readTree(content);
                } catch (Exception ignore) {
                    return null;
                }
            }

            LlmGradeResult r = new LlmGradeResult();
            r.success = true;
            r.llmCallId = call.getId();
            if (json.has("score")) r.score = json.get("score").asInt();
            if (json.has("confidence")) r.confidence = json.get("confidence").asDouble();
            if (json.has("needsReview")) r.needsReview = json.get("needsReview").asBoolean();
            if (json.has("comment")) r.comment = json.get("comment").asText();
            r.rawDetailJson = content;
            // 低置信度也要求复核
            if (r.confidence != null && r.confidence < 0.55) {
                r.needsReview = true;
            }
            return r;
        } catch (Exception e) {
            return null;
        }
    }

    private static class LlmGradeResult {
        boolean success;
        Long llmCallId;
        Integer score;
        Double confidence;
        boolean needsReview = true;
        String comment;
        String rawDetailJson;
    }
}
