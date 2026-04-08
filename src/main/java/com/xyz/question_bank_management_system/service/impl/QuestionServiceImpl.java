package com.xyz.question_bank_management_system.service.impl;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.common.enums.QuestionBankReviewStatusEnum;
import com.xyz.question_bank_management_system.common.enums.QuestionStatusEnum;
import com.xyz.question_bank_management_system.common.enums.QuestionTypeEnum;
import com.xyz.question_bank_management_system.config.LlmProperties;
import com.xyz.question_bank_management_system.dto.QuestionBankReviewRequest;
import com.xyz.question_bank_management_system.dto.QuestionCaseDTO;
import com.xyz.question_bank_management_system.dto.QuestionOptionDTO;
import com.xyz.question_bank_management_system.dto.QuestionSearchQuery;
import com.xyz.question_bank_management_system.dto.QuestionUpsertRequest;
import com.xyz.question_bank_management_system.entity.QbLlmCall;
import com.xyz.question_bank_management_system.entity.QbQuestion;
import com.xyz.question_bank_management_system.entity.QbQuestionCase;
import com.xyz.question_bank_management_system.entity.QbQuestionOption;
import com.xyz.question_bank_management_system.exception.BizException;
import com.xyz.question_bank_management_system.exception.ErrorCode;
import com.xyz.question_bank_management_system.mapper.QbLlmCallMapper;
import com.xyz.question_bank_management_system.mapper.QbQuestionCaseMapper;
import com.xyz.question_bank_management_system.mapper.QbQuestionMapper;
import com.xyz.question_bank_management_system.mapper.QbQuestionOptionMapper;
import com.xyz.question_bank_management_system.mapper.QbQuestionTagMapper;
import com.xyz.question_bank_management_system.mapper.SysRoleMapper;
import com.xyz.question_bank_management_system.service.AuditLogService;
import com.xyz.question_bank_management_system.service.LlmService;
import com.xyz.question_bank_management_system.service.QuestionService;
import com.xyz.question_bank_management_system.util.LlmPromptBuilder;
import com.xyz.question_bank_management_system.util.PageParamUtil;
import com.xyz.question_bank_management_system.util.TextRepairUtil;
import com.xyz.question_bank_management_system.vo.QuestionDetailVO;
import com.xyz.question_bank_management_system.vo.QuestionLlmAnalysisVO;
import com.xyz.question_bank_management_system.vo.QuestionLlmBatchResultVO;
import com.xyz.question_bank_management_system.vo.QuestionListItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private static final Set<String> ALLOWED_CHAPTERS = Set.of(
            "基础语法",
            "字符串处理",
            "数组与矩阵",
            "函数与递归",
            "指针基础",
            "数据结构基础",
            "文件输入输出",
            "综合应用"
    );

    private final QbQuestionMapper questionMapper;
    private final QbQuestionOptionMapper optionMapper;
    private final QbQuestionTagMapper questionTagMapper;
    private final QbQuestionCaseMapper caseMapper;
    private final QbLlmCallMapper llmCallMapper;
    private final SysRoleMapper roleMapper;
    private final LlmService llmService;
    private final LlmProperties llmProperties;
    private final AuditLogService auditLogService;
    @Qualifier("llmAnalysisExecutor")
    private final TaskExecutor llmAnalysisExecutor;

    @Override
    @Transactional
    public Long create(QuestionUpsertRequest request, Long creatorId) {
        QuestionTypeEnum type = validateQuestion(request);
        String chapter = normalizeAndValidateChapter(request.getChapter());
        String standardAnswer = resolveStandardAnswerForPersistence(request, type);

        QbQuestion q = new QbQuestion();
        q.setTitle(request.getTitle());
        q.setQuestionType(request.getQuestionType());
        q.setDifficulty(request.getDifficulty());
        q.setChapter(chapter);
        q.setStem(request.getStem());
        q.setStandardAnswer(standardAnswer);
        q.setAnswerFormat(request.getAnswerFormat());
        q.setAnalysisText(request.getAnalysisText());
        q.setAnalysisSource(request.getAnalysisSource());
        q.setStatus(request.getStatus());
        q.setCreatedBy(creatorId);
        applyBankReviewStateOnSave(q, request, isAdminUser(creatorId), false);

        questionMapper.insert(q);
        Long qid = q.getId();
        replaceOptionsAndTagsAndCases(qid, request);
        recordAudit(creatorId, "QUESTION_CREATE", "QUESTION", qid, null, questionAuditSnapshot(q, request.getTagIds()));
        return qid;
    }

    @Override
    @Transactional
    public void update(Long questionId, QuestionUpsertRequest request, Long actorId, boolean isAdmin) {
        QbQuestion exist = loadQuestionForManage(questionId, actorId, isAdmin);
        Map<String, Object> before = questionAuditSnapshot(exist, questionTagMapper.selectTagIdsByQuestionId(questionId));
        QuestionTypeEnum type = validateQuestion(request);
        String chapter = normalizeAndValidateChapter(request.getChapter());
        String standardAnswer = resolveStandardAnswerForPersistence(request, type);

        exist.setTitle(request.getTitle());
        exist.setQuestionType(request.getQuestionType());
        exist.setDifficulty(request.getDifficulty());
        exist.setChapter(chapter);
        exist.setStem(request.getStem());
        exist.setStandardAnswer(standardAnswer);
        exist.setAnswerFormat(request.getAnswerFormat());
        exist.setAnalysisText(request.getAnalysisText());
        exist.setAnalysisSource(request.getAnalysisSource());
        exist.setStatus(request.getStatus());

        boolean preserveBankReview = isAdmin && !Objects.equals(exist.getCreatedBy(), actorId);
        applyBankReviewStateOnSave(exist, request, isAdminUser(exist.getCreatedBy()), preserveBankReview);

        questionMapper.update(exist);
        replaceOptionsAndTagsAndCases(questionId, request);
        recordAudit(actorId, "QUESTION_UPDATE", "QUESTION", questionId, before, questionAuditSnapshot(exist, request.getTagIds()));
    }

    @Override
    public void delete(Long questionId, Long actorId, boolean isAdmin) {
        QbQuestion exist = loadQuestionForManage(questionId, actorId, isAdmin);
        Map<String, Object> before = questionAuditSnapshot(exist, questionTagMapper.selectTagIdsByQuestionId(questionId));
        questionMapper.softDelete(questionId);
        optionMapper.deleteByQuestionId(questionId);
        questionTagMapper.deleteByQuestionId(questionId);
        caseMapper.deleteByQuestionId(questionId);
        recordAudit(actorId, "QUESTION_DELETE", "QUESTION", questionId, before, null);
    }

    @Override
    public QuestionDetailVO detail(Long questionId, Long actorId, boolean isAdmin) {
        QbQuestion q = loadQuestionForManage(questionId, actorId, isAdmin);
        return buildQuestionDetail(q, true);
    }

    @Override
    public QuestionDetailVO detailForViewer(Long questionId, Long actorId, boolean isTeacher, boolean isAdmin) {
        QbQuestion q = questionMapper.selectById(questionId);
        if (q == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "棰樼洰涓嶅瓨鍦?");
        }
        if (isAdmin || (isTeacher && Objects.equals(q.getCreatedBy(), actorId))) {
            return buildQuestionDetail(q, true);
        }
        if (!Objects.equals(q.getStatus(), QuestionStatusEnum.PUBLISHED.getCode())
                || !Objects.equals(q.getBankReviewStatus(), QuestionBankReviewStatusEnum.APPROVED.getCode())) {
            throw BizException.of(ErrorCode.FORBIDDEN, "鏃犳潈鏌ョ湅璇ラ鐩?");
        }
        return buildQuestionDetail(q, true);
    }

    private QuestionDetailVO buildQuestionDetail(QbQuestion q, boolean includeLlmAnalyses) {
        QuestionDetailVO vo = new QuestionDetailVO();
        vo.setId(q.getId());
        vo.setTitle(q.getTitle());
        vo.setQuestionType(q.getQuestionType());
        vo.setDifficulty(q.getDifficulty());
        vo.setChapter(q.getChapter());
        vo.setStem(q.getStem());
        vo.setStandardAnswer(q.getStandardAnswer());
        vo.setAnswerFormat(q.getAnswerFormat());
        vo.setAnalysisText(q.getAnalysisText());
        vo.setAnalysisSource(q.getAnalysisSource());
        vo.setAnalysisLlmCallId(q.getAnalysisLlmCallId());
        vo.setStatus(q.getStatus());
        vo.setBankReviewStatus(q.getBankReviewStatus());
        vo.setBankReviewerId(q.getBankReviewerId());
        vo.setBankReviewedAt(q.getBankReviewedAt());
        vo.setBankReviewComment(q.getBankReviewComment());
        vo.setSubmitToBankReview(isSubmittedToBank(q.getBankReviewStatus()));
        vo.setCreatedBy(q.getCreatedBy());
        vo.setCreatedAt(q.getCreatedAt());
        vo.setUpdatedAt(q.getUpdatedAt());

        List<QbQuestionOption> opts = optionMapper.selectByQuestionId(q.getId());
        List<QuestionDetailVO.QuestionOptionVO> optVos = new ArrayList<>();
        for (QbQuestionOption o : opts) {
            QuestionDetailVO.QuestionOptionVO ov = new QuestionDetailVO.QuestionOptionVO();
            ov.setId(o.getId());
            ov.setOptionLabel(o.getOptionLabel());
            ov.setOptionContent(o.getOptionContent());
            ov.setIsCorrect(o.getIsCorrect());
            ov.setSortOrder(o.getSortOrder());
            optVos.add(ov);
        }
        vo.setOptions(optVos);

        vo.setTagIds(questionTagMapper.selectTagIdsByQuestionId(q.getId()));
        vo.setTagNames(questionTagMapper.selectTagNamesByQuestionId(q.getId()));

        List<QbQuestionCase> cases = caseMapper.selectByQuestionId(q.getId());
        List<QuestionDetailVO.QuestionCaseVO> caseVos = new ArrayList<>();
        for (QbQuestionCase c : cases) {
            QuestionDetailVO.QuestionCaseVO cv = new QuestionDetailVO.QuestionCaseVO();
            cv.setId(c.getId());
            cv.setCaseNo(c.getCaseNo());
            cv.setInputData(c.getInputData());
            cv.setExpectedOutput(c.getExpectedOutput());
            cv.setCaseScore(c.getCaseScore());
            cv.setIsSample(c.getIsSample());
            caseVos.add(cv);
        }
        vo.setCases(caseVos);
        vo.setLlmAnalyses(includeLlmAnalyses ? buildLatestLlmAnalyses(q.getId()) : List.of());
        return vo;
    }

    @Override
    public PageResponse<QuestionListItemVO> search(QuestionSearchQuery query, long page, long size) {
        long safePage = PageParamUtil.normalizePage(page);
        long safeSize = PageParamUtil.normalizeSize(size);
        long offset = PageParamUtil.offset(safePage, safeSize);

        List<QbQuestion> rows = questionMapper.search(query, offset, safeSize);
        long total = questionMapper.count(query);

        List<QuestionListItemVO> list = new ArrayList<>();
        for (QbQuestion q : rows) {
            QuestionListItemVO vo = new QuestionListItemVO();
            vo.setId(q.getId());
            vo.setTitle(TextRepairUtil.repairGbkUtf8Mojibake(q.getTitle()));
            vo.setQuestionType(q.getQuestionType());
            vo.setDifficulty(q.getDifficulty());
            vo.setChapter(TextRepairUtil.repairGbkUtf8Mojibake(q.getChapter()));
            vo.setStatus(q.getStatus());
            vo.setBankReviewStatus(q.getBankReviewStatus());
            vo.setBankReviewComment(TextRepairUtil.repairGbkUtf8Mojibake(q.getBankReviewComment()));
            vo.setCreatedBy(q.getCreatedBy());
            vo.setUpdatedAt(q.getUpdatedAt());
            vo.setTagIds(questionTagMapper.selectTagIdsByQuestionId(q.getId()));
            vo.setTagNames(questionTagMapper.selectTagNamesByQuestionId(q.getId()));
            list.add(vo);
        }
        return PageResponse.of(safePage, safeSize, total, list);
    }

    @Override
    public void publish(Long questionId, Long actorId, boolean isAdmin) {
        QbQuestion q = loadQuestionForManage(questionId, actorId, isAdmin);
        Map<String, Object> before = questionAuditSnapshot(q, questionTagMapper.selectTagIdsByQuestionId(questionId));

        QuestionTypeEnum type = QuestionTypeEnum.of(q.getQuestionType() == null ? -1 : q.getQuestionType());
        if (type == null || !type.isEnabledNow()) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "当前题型暂不可用");
        }

        if (type.isObjective() && (q.getStandardAnswer() == null || q.getStandardAnswer().isBlank())) {
            if (type == QuestionTypeEnum.SINGLE || type == QuestionTypeEnum.MULTIPLE) {
                String autoAnswer = deriveStandardAnswerFromEntities(optionMapper.selectByQuestionId(questionId), type);
                if (autoAnswer == null || autoAnswer.isBlank()) {
                    throw BizException.of(ErrorCode.PARAM_ERROR, "客观题必须设置正确选项");
                }
                q.setStandardAnswer(autoAnswer);
                questionMapper.update(q);
            } else {
                throw BizException.of(ErrorCode.PARAM_ERROR, "客观题必须提供标准答案");
            }
        }

        questionMapper.publish(questionId);
        q.setStatus(QuestionStatusEnum.PUBLISHED.getCode());
        if (isAdminUser(q.getCreatedBy())) {
            markApproved(q, q.getCreatedBy(), null, q.getCreatedBy());
            questionMapper.updateBankReview(q);
        }
        recordAudit(actorId, "QUESTION_PUBLISH", "QUESTION", questionId, before, questionAuditSnapshot(q, questionTagMapper.selectTagIdsByQuestionId(questionId)));
    }

    @Override
    @Transactional
    public void submitForBankReview(Long questionId, Long actorId) {
        QbQuestion q = loadQuestionForManage(questionId, actorId, false);
        Map<String, Object> before = questionAuditSnapshot(q, questionTagMapper.selectTagIdsByQuestionId(questionId));
        if (!Objects.equals(q.getStatus(), QuestionStatusEnum.PUBLISHED.getCode())) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "请先发布题目，再提交审核");
        }
        markPending(q);
        questionMapper.updateBankReview(q);
        recordAudit(actorId, "QUESTION_BANK_SUBMIT", "QUESTION", questionId, before, questionAuditSnapshot(q, questionTagMapper.selectTagIdsByQuestionId(questionId)));
    }

    @Override
    @Transactional
    public void cancelBankReview(Long questionId, Long actorId) {
        QbQuestion q = loadQuestionForManage(questionId, actorId, false);
        Map<String, Object> before = questionAuditSnapshot(q, questionTagMapper.selectTagIdsByQuestionId(questionId));
        markPrivate(q);
        questionMapper.updateBankReview(q);
        recordAudit(actorId, "QUESTION_BANK_CANCEL", "QUESTION", questionId, before, questionAuditSnapshot(q, questionTagMapper.selectTagIdsByQuestionId(questionId)));
    }

    @Override
    @Transactional
    public void reviewBankQuestion(Long questionId, QuestionBankReviewRequest request, Long reviewerId) {
        QbQuestion q = questionMapper.selectById(questionId);
        if (q == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "题目不存在");
        }
        Map<String, Object> before = questionAuditSnapshot(q, questionTagMapper.selectTagIdsByQuestionId(questionId));
        if (isAdminUser(q.getCreatedBy())) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "管理员创建的题目无需入库审核");
        }

        Integer reviewStatus = request.getReviewStatus();
        if (QuestionBankReviewStatusEnum.APPROVED.getCode() == reviewStatus) {
            if (!Objects.equals(q.getStatus(), QuestionStatusEnum.PUBLISHED.getCode())) {
                throw BizException.of(ErrorCode.PARAM_ERROR, "只有已发布题目才能进入总题库");
            }
            markApproved(q, reviewerId, request.getReviewComment(), reviewerId);
        } else if (QuestionBankReviewStatusEnum.REJECTED.getCode() == reviewStatus) {
            markRejected(q, reviewerId, request.getReviewComment());
        } else {
            throw BizException.of(ErrorCode.PARAM_ERROR, "审核状态只能是通过或驳回");
        }
        questionMapper.updateBankReview(q);
        recordAudit(reviewerId, "QUESTION_BANK_REVIEW", "QUESTION", questionId, before, questionAuditSnapshot(q, questionTagMapper.selectTagIdsByQuestionId(questionId)));
    }

    @Override
    @Transactional
    public QuestionLlmBatchResultVO generateAnalysisByLlm(Long questionId, String providerKey, Long actorId, boolean isAdmin) {
        QbQuestion q = loadQuestionForManage(questionId, actorId, isAdmin);
        LlmProperties.ModelProvider provider = providerKey == null || providerKey.isBlank()
                ? llmProperties.defaultProvider()
                : llmProperties.getProvider(providerKey);
        if (provider == null) {
            if (providerKey == null || providerKey.isBlank()) {
                throw BizException.of(ErrorCode.LLM_ERROR, "当前未启用任何大模型解析服务");
            }
            throw BizException.of(ErrorCode.PARAM_ERROR, "所选模型不存在或未启用");
        }
        if (llmProperties.questionAnalysisProviders().isEmpty()) {
            throw BizException.of(ErrorCode.LLM_ERROR, "当前未启用任何大模型解析服务");
        }

        String prompt = LlmPromptBuilder.buildQuestionAnalysisPrompt(
                q.getTitle(),
                q.getStem(),
                q.getStandardAnswer(),
                q.getAnalysisText());

        dispatchQuestionAnalysis(questionId, prompt, provider);

        QuestionLlmBatchResultVO result = new QuestionLlmBatchResultVO();
        result.setQuestionId(questionId);
        result.setProviderKey(provider.resolveKey());
        result.setProviderLabel(provider.resolveLabel());
        result.setRequestedCount(1);
        result.setRequestedModels(List.of(provider.resolveLabel()));
        return result;
    }

    private QuestionTypeEnum validateQuestion(QuestionUpsertRequest request) {
        QuestionTypeEnum type = QuestionTypeEnum.of(request.getQuestionType());
        if (type == null) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "未知题型");
        }
        if (!type.isEnabledNow()) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "当前题型暂不可用");
        }

        if (request.getDifficulty() != null && (request.getDifficulty() < 1 || request.getDifficulty() > 5)) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "难度必须在 1 到 5 之间");
        }

        if (type == QuestionTypeEnum.SINGLE || type == QuestionTypeEnum.MULTIPLE) {
            if (request.getOptions() == null || request.getOptions().isEmpty()) {
                throw BizException.of(ErrorCode.PARAM_ERROR, "选择题必须填写选项");
            }

            Set<String> seenLabels = new HashSet<>();
            for (QuestionOptionDTO option : request.getOptions()) {
                if (option == null || option.getOptionLabel() == null) {
                    continue;
                }
                String label = option.getOptionLabel().trim().toUpperCase();
                if (!seenLabels.add(label)) {
                    throw BizException.of(ErrorCode.PARAM_ERROR, "选项标识不能重复");
                }
            }

            long correctCount = request.getOptions().stream()
                    .filter(o -> o != null && o.getIsCorrect() != null && o.getIsCorrect() == 1)
                    .count();
            if (type == QuestionTypeEnum.SINGLE && correctCount != 1) {
                throw BizException.of(ErrorCode.PARAM_ERROR, "单选题必须且只能有一个正确选项");
            }
            if (type == QuestionTypeEnum.MULTIPLE && correctCount < 1) {
                throw BizException.of(ErrorCode.PARAM_ERROR, "多选题至少需要一个正确选项");
            }
        }

        String normalizedStandardAnswer = resolveStandardAnswerForPersistence(request, type);
        if (type.isObjective() && (normalizedStandardAnswer == null || normalizedStandardAnswer.isBlank())) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "客观题必须提供标准答案");
        }

        if (type == QuestionTypeEnum.CODE && request.getCases() != null) {
            for (QuestionCaseDTO c : request.getCases()) {
                if (c != null && c.getCaseScore() != null && c.getCaseScore() < 0) {
                    throw BizException.of(ErrorCode.PARAM_ERROR, "测试样例分值不能小于 0");
                }
            }
        }
        return type;
    }

    private String normalizeAndValidateChapter(String chapter) {
        if (chapter == null || chapter.isBlank()) {
            return null;
        }
        String normalized = chapter.trim();
        if (!ALLOWED_CHAPTERS.contains(normalized)) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "章节不合法: " + normalized);
        }
        return normalized;
    }

    private void replaceOptionsAndTagsAndCases(Long questionId, QuestionUpsertRequest request) {
        optionMapper.deleteByQuestionId(questionId);
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            List<QbQuestionOption> list = new ArrayList<>();
            for (QuestionOptionDTO o : request.getOptions()) {
                QbQuestionOption e = new QbQuestionOption();
                e.setQuestionId(questionId);
                e.setOptionLabel(o.getOptionLabel());
                e.setOptionContent(o.getOptionContent());
                e.setIsCorrect(o.getIsCorrect() == null ? 0 : o.getIsCorrect());
                e.setSortOrder(o.getSortOrder() == null ? 0 : o.getSortOrder());
                list.add(e);
            }
            optionMapper.batchInsert(list);
        }

        questionTagMapper.deleteByQuestionId(questionId);
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Long> uniqueTagIds = new ArrayList<>(new LinkedHashSet<>(request.getTagIds()));
            questionTagMapper.batchInsert(questionId, uniqueTagIds);
        }

        caseMapper.deleteByQuestionId(questionId);
        if (request.getCases() != null && !request.getCases().isEmpty()) {
            int no = 1;
            for (QuestionCaseDTO c : request.getCases()) {
                QbQuestionCase e = new QbQuestionCase();
                e.setQuestionId(questionId);
                e.setCaseNo(c.getCaseNo() == null ? no : c.getCaseNo());
                e.setInputData(c.getInputData());
                e.setExpectedOutput(c.getExpectedOutput());
                e.setCaseScore(c.getCaseScore() == null ? 0 : c.getCaseScore());
                e.setIsSample(c.getIsSample() == null ? 0 : c.getIsSample());
                caseMapper.insert(e);
                no++;
            }
        }
    }

    private void applyBankReviewStateOnSave(QbQuestion question,
                                            QuestionUpsertRequest request,
                                            boolean creatorIsAdmin,
                                            boolean preserveCurrentState) {
        if (preserveCurrentState) {
            return;
        }
        if (creatorIsAdmin) {
            if (Objects.equals(question.getStatus(), QuestionStatusEnum.PUBLISHED.getCode())) {
                markApproved(question, question.getCreatedBy(), null, question.getCreatedBy());
            } else {
                markPrivate(question);
            }
            return;
        }

        if (Boolean.TRUE.equals(request.getSubmitToBankReview())) {
            if (!Objects.equals(question.getStatus(), QuestionStatusEnum.PUBLISHED.getCode())) {
                throw BizException.of(ErrorCode.PARAM_ERROR, "请先发布题目，再提交总题库审核");
            }
            markPending(question);
        } else {
            markPrivate(question);
        }
    }

    private void markPrivate(QbQuestion question) {
        question.setBankReviewStatus(QuestionBankReviewStatusEnum.PRIVATE_ONLY.getCode());
        question.setBankReviewerId(null);
        question.setBankReviewedAt(null);
        question.setBankReviewComment(null);
    }

    private void markPending(QbQuestion question) {
        question.setBankReviewStatus(QuestionBankReviewStatusEnum.PENDING.getCode());
        question.setBankReviewerId(null);
        question.setBankReviewedAt(null);
        question.setBankReviewComment(null);
    }

    private void markApproved(QbQuestion question, Long reviewerId, String reviewComment, Long defaultReviewerId) {
        question.setBankReviewStatus(QuestionBankReviewStatusEnum.APPROVED.getCode());
        question.setBankReviewerId(reviewerId == null ? defaultReviewerId : reviewerId);
        question.setBankReviewedAt(LocalDateTime.now());
        question.setBankReviewComment(trimToNull(reviewComment));
    }

    private void markRejected(QbQuestion question, Long reviewerId, String reviewComment) {
        question.setBankReviewStatus(QuestionBankReviewStatusEnum.REJECTED.getCode());
        question.setBankReviewerId(reviewerId);
        question.setBankReviewedAt(LocalDateTime.now());
        question.setBankReviewComment(trimToNull(reviewComment));
    }

    private boolean isSubmittedToBank(Integer bankReviewStatus) {
        return Objects.equals(bankReviewStatus, QuestionBankReviewStatusEnum.PENDING.getCode())
                || Objects.equals(bankReviewStatus, QuestionBankReviewStatusEnum.APPROVED.getCode());
    }

    private boolean isAdminUser(Long userId) {
        if (userId == null) {
            return false;
        }
        return "ADMIN".equalsIgnoreCase(roleMapper.selectRoleCodeByUserId(userId));
    }

    private String resolveStandardAnswerForPersistence(QuestionUpsertRequest request, QuestionTypeEnum type) {
        String manual = trimToNull(request.getStandardAnswer());
        if (type == QuestionTypeEnum.SINGLE || type == QuestionTypeEnum.MULTIPLE) {
            String derived = deriveStandardAnswerFromDtos(request.getOptions(), type);
            return derived == null ? manual : derived;
        }
        return manual;
    }

    private String deriveStandardAnswerFromDtos(List<QuestionOptionDTO> options, QuestionTypeEnum type) {
        if (options == null || options.isEmpty()) {
            return null;
        }
        List<String> correctLabels = options.stream()
                .filter(Objects::nonNull)
                .filter(o -> o.getIsCorrect() != null && o.getIsCorrect() == 1)
                .sorted(Comparator
                        .comparing((QuestionOptionDTO o) -> o.getSortOrder() == null ? Integer.MAX_VALUE : o.getSortOrder())
                        .thenComparing(o -> normalizeOptionLabel(o.getOptionLabel())))
                .map(o -> normalizeOptionLabel(o.getOptionLabel()))
                .filter(Objects::nonNull)
                .toList();
        if (correctLabels.isEmpty()) {
            return null;
        }
        if (type == QuestionTypeEnum.SINGLE) {
            return correctLabels.get(0);
        }
        return String.join(",", correctLabels);
    }

    private String deriveStandardAnswerFromEntities(List<QbQuestionOption> options, QuestionTypeEnum type) {
        if (options == null || options.isEmpty()) {
            return null;
        }
        List<String> correctLabels = options.stream()
                .filter(Objects::nonNull)
                .filter(o -> o.getIsCorrect() != null && o.getIsCorrect() == 1)
                .sorted(Comparator
                        .comparing((QbQuestionOption o) -> o.getSortOrder() == null ? Integer.MAX_VALUE : o.getSortOrder())
                        .thenComparing(o -> normalizeOptionLabel(o.getOptionLabel())))
                .map(o -> normalizeOptionLabel(o.getOptionLabel()))
                .filter(Objects::nonNull)
                .toList();
        if (correctLabels.isEmpty()) {
            return null;
        }
        if (type == QuestionTypeEnum.SINGLE) {
            return correctLabels.get(0);
        }
        return String.join(",", correctLabels);
    }

    private String normalizeOptionLabel(String label) {
        if (label == null) {
            return null;
        }
        String normalized = label.trim().toUpperCase();
        return normalized.isEmpty() ? null : normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Map<String, Object> questionAuditSnapshot(QbQuestion question, List<Long> tagIds) {
        Map<String, Object> snapshot = new java.util.LinkedHashMap<>();
        snapshot.put("id", question.getId());
        snapshot.put("title", question.getTitle());
        snapshot.put("questionType", question.getQuestionType());
        snapshot.put("difficulty", question.getDifficulty());
        snapshot.put("chapter", question.getChapter());
        snapshot.put("status", question.getStatus());
        snapshot.put("bankReviewStatus", question.getBankReviewStatus());
        snapshot.put("bankReviewComment", question.getBankReviewComment());
        snapshot.put("createdBy", question.getCreatedBy());
        snapshot.put("tagIds", tagIds);
        return snapshot;
    }

    private void recordAudit(Long userId,
                             String action,
                             String entityType,
                             Long entityId,
                             Object beforeData,
                             Object afterData) {
        if (auditLogService == null) {
            return;
        }
        auditLogService.record(userId, action, entityType, entityId, beforeData, afterData);
    }

    private QbQuestion loadQuestionForManage(Long questionId, Long actorId, boolean isAdmin) {
        QbQuestion q = questionMapper.selectById(questionId);
        if (q == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "题目不存在");
        }
        if (!isAdmin && !Objects.equals(q.getCreatedBy(), actorId)) {
            throw BizException.of(ErrorCode.FORBIDDEN, "无权管理该题目");
        }
        return q;
    }

    private void dispatchQuestionAnalysis(Long questionId, String prompt, LlmProperties.ModelProvider provider) {
        Runnable task = () -> llmService.chatCompletion(1, questionId, prompt, provider.getKey());
        try {
            llmAnalysisExecutor.execute(task);
        } catch (RuntimeException ex) {
            log.warn("提交题目解析任务失败，改为当前线程执行，questionId={}, provider={}", questionId, provider.getKey(), ex);
            task.run();
        }
    }

    private List<QuestionLlmAnalysisVO> buildLatestLlmAnalyses(Long questionId) {
        List<QbLlmCall> calls = llmCallMapper.selectByBiz(1, questionId);
        List<QuestionLlmAnalysisVO> analyses = new ArrayList<>();
        for (LlmProperties.ModelProvider provider : llmProperties.questionAnalysisProviders()) {
            analyses.add(buildQuestionLlmAnalysis(provider, calls));
        }
        return analyses;
    }

    private QuestionLlmAnalysisVO buildQuestionLlmAnalysis(LlmProperties.ModelProvider provider, List<QbLlmCall> calls) {
        QuestionLlmAnalysisVO vo = new QuestionLlmAnalysisVO();
        vo.setAnalysisKey(provider.resolveKey());
        vo.setAnalysisLabel(provider.resolveLabel());
        vo.setModelName(provider.getModel());
        vo.setHasAnalysis(false);

        QbLlmCall latestCall = findLatestCall(provider, calls, null);
        QbLlmCall latestSuccessfulCall = findLatestCall(provider, calls, 1);
        if (latestCall == null) {
            vo.setCallStatus(-1);
            vo.setAnalysisText("尚未生成解析");
            return vo;
        }

        if (latestSuccessfulCall != null) {
            vo.setHasAnalysis(true);
            fillAnalysisContent(vo, latestSuccessfulCall);
            if (Objects.equals(latestCall.getId(), latestSuccessfulCall.getId())
                    || Objects.equals(latestCall.getCallStatus(), 1)) {
                vo.setCallStatus(1);
                return vo;
            }
            if (Objects.equals(latestCall.getCallStatus(), 0)) {
                vo.setCallStatus(0);
                vo.setErrorMessage("正在重新生成，当前展示上一次成功解析");
                return vo;
            }
            vo.setCallStatus(2);
            vo.setErrorMessage(trimToNull(latestCall.getResponseText()));
            if (vo.getErrorMessage() == null) {
                vo.setErrorMessage("最近一次生成失败，当前展示上一次成功解析");
            }
            return vo;
        }

        vo.setLlmCallId(latestCall.getId());
        vo.setCallStatus(latestCall.getCallStatus());
        vo.setLatencyMs(latestCall.getLatencyMs());
        vo.setCreatedAt(latestCall.getCreatedAt());

        if (latestCall.getCallStatus() != null && latestCall.getCallStatus() == 0) {
            vo.setAnalysisText("解析生成中，请稍后刷新查看结果");
            return vo;
        }

        vo.setAnalysisText("解析生成失败");
        vo.setErrorMessage(trimToNull(latestCall.getResponseText()));
        return vo;
    }

    private void fillAnalysisContent(QuestionLlmAnalysisVO vo, QbLlmCall call) {
        vo.setLlmCallId(call.getId());
        vo.setLatencyMs(call.getLatencyMs());
        vo.setCreatedAt(call.getCreatedAt());
        String content = llmService.extractContent(call.getResponseText());
        if (content == null || content.isBlank()) {
            vo.setAnalysisText("解析已生成，但暂未提取到正文内容");
            return;
        }
        vo.setAnalysisText(content.trim());
    }

    private QbLlmCall findLatestCall(LlmProperties.ModelProvider provider, List<QbLlmCall> calls, Integer expectedStatus) {
        if (calls == null || calls.isEmpty()) {
            return null;
        }
        QbLlmCall latestCall = null;
        for (QbLlmCall call : calls) {
            if (call == null || !matchesProvider(provider, call.getModelName())) {
                continue;
            }
            if (expectedStatus != null && !Objects.equals(expectedStatus, call.getCallStatus())) {
                continue;
            }
            if (latestCall == null || isNewerCall(call, latestCall)) {
                latestCall = call;
            }
        }
        return latestCall;
    }

    private boolean isNewerCall(QbLlmCall candidate, QbLlmCall current) {
        if (candidate == null) {
            return false;
        }
        if (current == null) {
            return true;
        }
        if (candidate.getId() != null && current.getId() != null) {
            return candidate.getId() > current.getId();
        }
        if (candidate.getCreatedAt() != null && current.getCreatedAt() != null) {
            return candidate.getCreatedAt().isAfter(current.getCreatedAt());
        }
        return candidate.getCreatedAt() != null || current.getCreatedAt() == null;
    }

    private boolean matchesProvider(LlmProperties.ModelProvider provider, String modelName) {
        if (provider == null || modelName == null || modelName.isBlank()) {
            return false;
        }
        String normalizedModelName = modelName.trim().toLowerCase();
        if (provider.getModel() != null && normalizedModelName.equals(provider.getModel().trim().toLowerCase())) {
            return true;
        }
        if (provider.getKey() != null && normalizedModelName.equals(provider.getKey().trim().toLowerCase())) {
            return true;
        }
        return provider.getLabel() != null && normalizedModelName.equals(provider.getLabel().trim().toLowerCase());
    }
}
