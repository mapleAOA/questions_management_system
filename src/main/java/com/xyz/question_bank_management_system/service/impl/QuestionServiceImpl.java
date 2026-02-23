package com.xyz.question_bank_management_system.service.impl;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.common.enums.QuestionTypeEnum;
import com.xyz.question_bank_management_system.dto.*;
import com.xyz.question_bank_management_system.entity.*;
import com.xyz.question_bank_management_system.exception.BizException;
import com.xyz.question_bank_management_system.exception.ErrorCode;
import com.xyz.question_bank_management_system.mapper.*;
import com.xyz.question_bank_management_system.service.LlmService;
import com.xyz.question_bank_management_system.service.QuestionService;
import com.xyz.question_bank_management_system.vo.QuestionDetailVO;
import com.xyz.question_bank_management_system.vo.QuestionListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QbQuestionMapper questionMapper;
    private final QbQuestionOptionMapper optionMapper;
    private final QbQuestionTagMapper questionTagMapper;
    private final QbQuestionCaseMapper caseMapper;
    private final LlmService llmService;

    @Override
    @Transactional
    public Long create(QuestionUpsertRequest request, Long creatorId) {
        QbQuestion q = new QbQuestion();
        q.setTitle(request.getTitle());
        q.setQuestionType(request.getQuestionType());
        q.setDifficulty(request.getDifficulty());
        q.setChapter(request.getChapter());
        q.setStem(request.getStem());
        q.setStandardAnswer(request.getStandardAnswer());
        q.setAnswerFormat(request.getAnswerFormat());
        q.setAnalysisText(request.getAnalysisText());
        q.setAnalysisSource(request.getAnalysisSource());
        q.setStatus(request.getStatus());
        q.setCreatedBy(creatorId);

        validateQuestion(request);

        questionMapper.insert(q);
        Long qid = q.getId();

        replaceOptionsAndTagsAndCases(qid, request);
        return qid;
    }

    @Override
    @Transactional
    public void update(Long questionId, QuestionUpsertRequest request) {
        QbQuestion exist = questionMapper.selectById(questionId);
        if (exist == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "题目不存在");
        }

        validateQuestion(request);

        exist.setTitle(request.getTitle());
        exist.setQuestionType(request.getQuestionType());
        exist.setDifficulty(request.getDifficulty());
        exist.setChapter(request.getChapter());
        exist.setStem(request.getStem());
        exist.setStandardAnswer(request.getStandardAnswer());
        exist.setAnswerFormat(request.getAnswerFormat());
        exist.setAnalysisText(request.getAnalysisText());
        exist.setAnalysisSource(request.getAnalysisSource());
        exist.setStatus(request.getStatus());

        questionMapper.update(exist);

        replaceOptionsAndTagsAndCases(questionId, request);
    }

    private void validateQuestion(QuestionUpsertRequest request) {
        QuestionTypeEnum type = QuestionTypeEnum.of(request.getQuestionType());
        if (type == null) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "未知题型");
        }
        if (type == QuestionTypeEnum.SINGLE || type == QuestionTypeEnum.MULTIPLE) {
            if (request.getOptions() == null || request.getOptions().isEmpty()) {
                throw BizException.of(ErrorCode.PARAM_ERROR, "选择题必须提供options");
            }
        }
        if (type == QuestionTypeEnum.CODE || type == QuestionTypeEnum.CODE_READING) {
            // 可选：cases
        }
    }

    private void replaceOptionsAndTagsAndCases(Long questionId, QuestionUpsertRequest request) {
        // options
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

        // tags
        questionTagMapper.deleteByQuestionId(questionId);
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            questionTagMapper.batchInsert(questionId, request.getTagIds());
        }

        // cases (简单做法：全量替换)
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
    public void delete(Long questionId) {
        QbQuestion exist = questionMapper.selectById(questionId);
        if (exist == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "题目不存在");
        }
        questionMapper.softDelete(questionId);
        // 关联表可不删除，保持历史；这里按需要清理
        optionMapper.deleteByQuestionId(questionId);
        questionTagMapper.deleteByQuestionId(questionId);
        caseMapper.deleteByQuestionId(questionId);
    }

    @Override
    public QuestionDetailVO detail(Long questionId) {
        QbQuestion q = questionMapper.selectById(questionId);
        if (q == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "题目不存在");
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

        // options
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
        long offset = (page - 1) * size;
        List<QbQuestion> rows = questionMapper.search(query, offset, size);
        long total = questionMapper.count(query);

        List<QuestionListItemVO> list = new ArrayList<>();
        for (QbQuestion q : rows) {
            QuestionListItemVO vo = new QuestionListItemVO();
            vo.setId(q.getId());
            vo.setTitle(q.getTitle());
            vo.setQuestionType(q.getQuestionType());
            vo.setDifficulty(q.getDifficulty());
            vo.setChapter(q.getChapter());
            vo.setStatus(q.getStatus());
            vo.setCreatedBy(q.getCreatedBy());
            vo.setUpdatedAt(q.getUpdatedAt());
            vo.setTagIds(questionTagMapper.selectTagIdsByQuestionId(q.getId()));
            list.add(vo);
        }
        return PageResponse.of(page, size, total, list);
    }

    @Override
    public void publish(Long questionId) {
        QbQuestion q = questionMapper.selectById(questionId);
        if (q == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "题目不存在");
        }
        questionMapper.publish(questionId);
    }

    @Override
    @Transactional
    public Long generateAnalysisByLlm(Long questionId) {
        QbQuestion q = questionMapper.selectById(questionId);
        if (q == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "题目不存在");
        }

        String prompt = "请为下面题目生成详细解析（中文），输出结构如下：\n" +
                "1) 考点\n2) 解题思路\n3) 参考答案（如已有标准答案请保持一致）\n4) 易错点\n\n" +
                "题目标题：" + q.getTitle() + "\n" +
                "题干：" + q.getStem() + "\n" +
                (q.getStandardAnswer() == null ? "" : ("标准答案：" + q.getStandardAnswer() + "\n"));

        var call = llmService.chatCompletion(1, questionId, prompt);
        String content = llmService.extractContent(call.getResponseText());
        if (content == null || content.isBlank()) {
            //LC待写
            throw BizException.of(ErrorCode.LLM_ERROR, "LLM未返回有效内容");
        }

        q.setAnalysisText(content);
        q.setAnalysisSource(2);
        q.setAnalysisLlmCallId(call.getId());
        questionMapper.update(q);
        return call.getId();
    }
}
