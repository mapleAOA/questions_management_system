package com.xyz.question_bank_management_system.service.impl;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.common.enums.QuestionTypeEnum;
import com.xyz.question_bank_management_system.dto.QuestionCaseDTO;
import com.xyz.question_bank_management_system.dto.QuestionCaseUpsertRequest;
import com.xyz.question_bank_management_system.dto.QuestionOptionDTO;
import com.xyz.question_bank_management_system.dto.QuestionSearchQuery;
import com.xyz.question_bank_management_system.dto.QuestionUpsertRequest;
import com.xyz.question_bank_management_system.entity.QbQuestion;
import com.xyz.question_bank_management_system.entity.QbQuestionCase;
import com.xyz.question_bank_management_system.entity.QbQuestionOption;
import com.xyz.question_bank_management_system.exception.BizException;
import com.xyz.question_bank_management_system.exception.ErrorCode;
import com.xyz.question_bank_management_system.mapper.QbQuestionCaseMapper;
import com.xyz.question_bank_management_system.mapper.QbQuestionMapper;
import com.xyz.question_bank_management_system.mapper.QbQuestionOptionMapper;
import com.xyz.question_bank_management_system.mapper.QbQuestionTagMapper;
import com.xyz.question_bank_management_system.service.LlmService;
import com.xyz.question_bank_management_system.service.QuestionService;
import com.xyz.question_bank_management_system.util.PageParamUtil;
import com.xyz.question_bank_management_system.util.TextRepairUtil;
import com.xyz.question_bank_management_system.vo.QuestionDetailVO;
import com.xyz.question_bank_management_system.vo.QuestionListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private static final Set<String> ALLOWED_CHAPTERS = Set.of(
            "\u57FA\u7840\u8BED\u6CD5",
            "\u5B57\u7B26\u4E32\u5904\u7406",
            "\u6570\u7EC4\u4E0E\u77E9\u9635",
            "\u51FD\u6570\u4E0E\u9012\u5F52",
            "\u6307\u9488\u57FA\u7840",
            "\u6570\u636E\u7ED3\u6784\u57FA\u7840",
            "\u6587\u4EF6\u8F93\u5165\u8F93\u51FA",
            "\u7EFC\u5408\u5E94\u7528"
    );

    private final QbQuestionMapper questionMapper;
    private final QbQuestionOptionMapper optionMapper;
    private final QbQuestionTagMapper questionTagMapper;
    private final QbQuestionCaseMapper caseMapper;
    private final LlmService llmService;

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

        questionMapper.insert(q);
        Long qid = q.getId();
        replaceOptionsAndTagsAndCases(qid, request);
        return qid;
    }

    @Override
    @Transactional
    public void update(Long questionId, QuestionUpsertRequest request, Long actorId, boolean isAdmin) {
        QbQuestion exist = loadQuestionForManage(questionId, actorId, isAdmin);
        if (exist == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "question not found");
        }

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

        questionMapper.update(exist);
        replaceOptionsAndTagsAndCases(questionId, request);
    }

    private QuestionTypeEnum validateQuestion(QuestionUpsertRequest request) {
        QuestionTypeEnum type = QuestionTypeEnum.of(request.getQuestionType());
        if (type == null) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "unknown question type");
        }
        if (!type.isEnabledNow()) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "question type is disabled");
        }

        if (request.getDifficulty() != null && (request.getDifficulty() < 1 || request.getDifficulty() > 5)) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "difficulty must be in [1,5]");
        }

        if (type == QuestionTypeEnum.SINGLE || type == QuestionTypeEnum.MULTIPLE) {
            if (request.getOptions() == null || request.getOptions().isEmpty()) {
                throw BizException.of(ErrorCode.PARAM_ERROR, "options are required for choice questions");
            }

            Set<String> seenLabels = new HashSet<>();
            for (QuestionOptionDTO option : request.getOptions()) {
                if (option == null || option.getOptionLabel() == null) {
                    continue;
                }
                String label = option.getOptionLabel().trim().toUpperCase();
                if (!seenLabels.add(label)) {
                    throw BizException.of(ErrorCode.PARAM_ERROR, "duplicate option label");
                }
            }

            long correctCount = request.getOptions().stream()
                    .filter(o -> o != null && o.getIsCorrect() != null && o.getIsCorrect() == 1)
                    .count();
            if (type == QuestionTypeEnum.SINGLE && correctCount != 1) {
                throw BizException.of(ErrorCode.PARAM_ERROR, "single choice must have exactly one correct option");
            }
            if (type == QuestionTypeEnum.MULTIPLE && correctCount < 1) {
                throw BizException.of(ErrorCode.PARAM_ERROR, "multiple choice needs at least one correct option");
            }
        }

        String normalizedStandardAnswer = resolveStandardAnswerForPersistence(request, type);
        if (type.isObjective() && (normalizedStandardAnswer == null || normalizedStandardAnswer.isBlank())) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "objective question requires standard answer");
        }

        if (type == QuestionTypeEnum.CODE && request.getCases() != null) {
            for (QuestionCaseDTO c : request.getCases()) {
                if (c != null && c.getCaseScore() != null && c.getCaseScore() < 0) {
                    throw BizException.of(ErrorCode.PARAM_ERROR, "caseScore cannot be negative");
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
            throw BizException.of(ErrorCode.PARAM_ERROR, "invalid chapter: " + normalized);
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

    @Override
    public void delete(Long questionId, Long actorId, boolean isAdmin) {
        QbQuestion exist = loadQuestionForManage(questionId, actorId, isAdmin);
        if (exist == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "question not found");
        }

        questionMapper.softDelete(questionId);
        optionMapper.deleteByQuestionId(questionId);
        questionTagMapper.deleteByQuestionId(questionId);
        caseMapper.deleteByQuestionId(questionId);
    }

    @Override
    public QuestionDetailVO detail(Long questionId, Long actorId, boolean isAdmin) {
        QbQuestion q = loadQuestionForManage(questionId, actorId, isAdmin);
        if (q == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "question not found");
        }

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
        vo.setCreatedBy(q.getCreatedBy());
        vo.setCreatedAt(q.getCreatedAt());
        vo.setUpdatedAt(q.getUpdatedAt());

        List<QbQuestionOption> opts = optionMapper.selectByQuestionId(questionId);
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

        vo.setTagIds(questionTagMapper.selectTagIdsByQuestionId(questionId));
        vo.setTagNames(questionTagMapper.selectTagNamesByQuestionId(questionId));

        List<QbQuestionCase> cases = caseMapper.selectByQuestionId(questionId);
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
            vo.setCreatedBy(q.getCreatedBy());
            vo.setUpdatedAt(q.getUpdatedAt());
            vo.setTagIds(questionTagMapper.selectTagIdsByQuestionId(q.getId()));
            vo.setTagNames(questionTagMapper.selectTagNamesByQuestionId(q.getId()));
            list.add(vo);
        }
        return PageResponse.of(safePage, safeSize, total, list);
    }

    @Override
    public List<QbQuestionCase> listCases(Long questionId, Long actorId, boolean isAdmin) {
        QbQuestion q = loadQuestionForManage(questionId, actorId, isAdmin);
        if (q == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "question not found");
        }
        return caseMapper.selectByQuestionId(questionId);
    }

    @Override
    @Transactional
    public Long upsertCase(Long questionId, QuestionCaseUpsertRequest request, Long actorId, boolean isAdmin) {
        QbQuestion q = loadQuestionForManage(questionId, actorId, isAdmin);
        if (q == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "question not found");
        }

        QbQuestionCase existed = caseMapper.selectByQuestionAndCaseNo(questionId, request.getCaseNo());
        if (existed != null) {
            existed.setInputData(request.getInputData());
            existed.setExpectedOutput(request.getExpectedOutput());
            existed.setCaseScore(request.getCaseScore() == null ? 0 : request.getCaseScore());
            existed.setIsSample(Boolean.TRUE.equals(request.getIsSample()) ? 1 : 0);
            caseMapper.update(existed);
            return existed.getId();
        }

        QbQuestionCase c = new QbQuestionCase();
        c.setQuestionId(questionId);
        c.setCaseNo(request.getCaseNo());
        c.setInputData(request.getInputData());
        c.setExpectedOutput(request.getExpectedOutput());
        c.setCaseScore(request.getCaseScore() == null ? 0 : request.getCaseScore());
        c.setIsSample(Boolean.TRUE.equals(request.getIsSample()) ? 1 : 0);
        caseMapper.insert(c);
        return c.getId();
    }

    @Override
    @Transactional
    public void deleteCase(Long caseId, Long actorId, boolean isAdmin) {
        QbQuestionCase existed = caseMapper.selectById(caseId);
        if (existed == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "question not found");
        }
        loadQuestionForManage(existed.getQuestionId(), actorId, isAdmin);
        caseMapper.deleteById(caseId);
    }

    @Override
    public void publish(Long questionId, Long actorId, boolean isAdmin) {
        QbQuestion q = loadQuestionForManage(questionId, actorId, isAdmin);
        if (q == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "question not found");
        }

        QuestionTypeEnum type = QuestionTypeEnum.of(q.getQuestionType() == null ? -1 : q.getQuestionType());
        if (type == null || !type.isEnabledNow()) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "question type is disabled");
        }

        if (type.isObjective() && (q.getStandardAnswer() == null || q.getStandardAnswer().isBlank())) {
            if (type == QuestionTypeEnum.SINGLE || type == QuestionTypeEnum.MULTIPLE) {
                String autoAnswer = deriveStandardAnswerFromEntities(optionMapper.selectByQuestionId(questionId), type);
                if (autoAnswer == null || autoAnswer.isBlank()) {
                    throw BizException.of(ErrorCode.PARAM_ERROR, "objective question requires correct options");
                }
                q.setStandardAnswer(autoAnswer);
                questionMapper.update(q);
            } else {
                throw BizException.of(ErrorCode.PARAM_ERROR, "objective question requires standard answer");
            }
        }
        questionMapper.publish(questionId);
    }

    @Override
    @Transactional
    public Long generateAnalysisByLlm(Long questionId, Long actorId, boolean isAdmin) {
        QbQuestion q = loadQuestionForManage(questionId, actorId, isAdmin);
        if (q == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "question not found");
        }

        String prompt = "Please generate a detailed explanation in Chinese for this question.\\n"
                + "Output structure:\\n1) Key points\\n2) Solution idea\\n3) Reference approach\\n4) Common mistakes\\n\\n"
                + "Question title: " + q.getTitle() + "\\n"
                + "Stem: " + q.getStem() + "\\n";
        var call = llmService.chatCompletion(1, questionId, prompt);
        String content = llmService.extractContent(call.getResponseText());
        if (content == null || content.isBlank()) {
            throw BizException.of(ErrorCode.LLM_ERROR, "LLM returned empty content");
        }

        q.setAnalysisText(content);
        q.setAnalysisSource(2);
        q.setAnalysisLlmCallId(call.getId());
        questionMapper.update(q);
        return call.getId();
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

    private QbQuestion loadQuestionForManage(Long questionId, Long actorId, boolean isAdmin) {
        QbQuestion q = questionMapper.selectById(questionId);
        if (q == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "question not found");
        }
        if (!isAdmin && !Objects.equals(q.getCreatedBy(), actorId)) {
            throw BizException.of(ErrorCode.FORBIDDEN, "no permission to manage this question");
        }
        return q;
    }
}
