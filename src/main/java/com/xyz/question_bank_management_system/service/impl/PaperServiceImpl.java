package com.xyz.question_bank_management_system.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.dto.PaperAddQuestionRequest;
import com.xyz.question_bank_management_system.dto.PaperQuestionUpdateRequest;
import com.xyz.question_bank_management_system.dto.PaperUpsertRequest;
import com.xyz.question_bank_management_system.entity.QbPaper;
import com.xyz.question_bank_management_system.entity.QbPaperQuestion;
import com.xyz.question_bank_management_system.entity.QbQuestion;
import com.xyz.question_bank_management_system.entity.QbQuestionCase;
import com.xyz.question_bank_management_system.entity.QbQuestionOption;
import com.xyz.question_bank_management_system.exception.BizException;
import com.xyz.question_bank_management_system.exception.ErrorCode;
import com.xyz.question_bank_management_system.mapper.QbPaperMapper;
import com.xyz.question_bank_management_system.mapper.QbPaperQuestionMapper;
import com.xyz.question_bank_management_system.mapper.QbQuestionCaseMapper;
import com.xyz.question_bank_management_system.mapper.QbQuestionMapper;
import com.xyz.question_bank_management_system.mapper.QbQuestionOptionMapper;
import com.xyz.question_bank_management_system.mapper.QbQuestionTagMapper;
import com.xyz.question_bank_management_system.mapper.SysRoleMapper;
import com.xyz.question_bank_management_system.service.PaperService;
import com.xyz.question_bank_management_system.util.HashUtil;
import com.xyz.question_bank_management_system.util.PageParamUtil;
import com.xyz.question_bank_management_system.vo.PaperDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaperServiceImpl implements PaperService {

    private final QbPaperMapper paperMapper;
    private final QbPaperQuestionMapper paperQuestionMapper;
    private final QbQuestionMapper questionMapper;
    private final QbQuestionOptionMapper optionMapper;
    private final QbQuestionCaseMapper caseMapper;
    private final QbQuestionTagMapper questionTagMapper;
    private final SysRoleMapper roleMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public Long create(PaperUpsertRequest request, Long creatorId) {
        QbPaper p = new QbPaper();
        p.setPaperTitle(request.getPaperTitle());
        p.setPaperDesc(request.getPaperDesc());
        p.setPaperType(request.getPaperType());
        p.setStatus(request.getStatus());
        p.setRuleJson(request.getRuleJson());
        p.setTotalScore(0);
        p.setCreatorId(creatorId);
        paperMapper.insert(p);
        return p.getId();
    }

    @Override
    public void update(Long paperId, PaperUpsertRequest request, Long actorId, boolean isAdmin) {
        QbPaper p = loadPaperForManage(paperId, actorId, isAdmin);
        p.setPaperTitle(request.getPaperTitle());
        p.setPaperDesc(request.getPaperDesc());
        p.setPaperType(request.getPaperType());
        p.setStatus(request.getStatus());
        p.setRuleJson(request.getRuleJson());
        paperMapper.update(p);
    }

    @Override
    @Transactional
    public void delete(Long paperId, Long actorId, boolean isAdmin) {
        loadPaperForManage(paperId, actorId, isAdmin);
        paperMapper.softDelete(paperId);
        paperQuestionMapper.deleteByPaperId(paperId);
    }

    @Override
    public PageResponse<QbPaper> page(long page, long size, Long actorId, boolean isAdmin) {
        long safePage = PageParamUtil.normalizePage(page);
        long safeSize = PageParamUtil.normalizeSize(size);
        long offset = PageParamUtil.offset(safePage, safeSize);

        List<QbPaper> rows;
        long total;
        if (isAdmin) {
            rows = paperMapper.page(offset, safeSize);
            total = paperMapper.countAll();
        } else {
            rows = paperMapper.pageByCreator(actorId, offset, safeSize);
            total = paperMapper.countByCreator(actorId);
        }
        return PageResponse.of(safePage, safeSize, total, rows);
    }

    @Override
    public PaperDetailVO detail(Long paperId, Long actorId, boolean isAdmin) {
        QbPaper p = loadPaperForManage(paperId, actorId, isAdmin);

        PaperDetailVO vo = new PaperDetailVO();
        vo.setId(p.getId());
        vo.setPaperTitle(p.getPaperTitle());
        vo.setPaperDesc(p.getPaperDesc());
        vo.setPaperType(p.getPaperType());
        vo.setTotalScore(p.getTotalScore());
        vo.setRuleJson(p.getRuleJson());
        vo.setStatus(p.getStatus());
        vo.setCreatorId(p.getCreatorId());
        vo.setCreatedAt(p.getCreatedAt());
        vo.setUpdatedAt(p.getUpdatedAt());

        List<QbPaperQuestion> pqs = paperQuestionMapper.selectByPaperId(paperId);
        List<PaperDetailVO.PaperQuestionVO> qvos = new ArrayList<>();
        for (QbPaperQuestion pq : pqs) {
            PaperDetailVO.PaperQuestionVO qvo = new PaperDetailVO.PaperQuestionVO();
            qvo.setId(pq.getId());
            qvo.setQuestionId(pq.getQuestionId());
            qvo.setOrderNo(pq.getOrderNo());
            qvo.setScore(pq.getScore());
            qvo.setSnapshotJson(pq.getSnapshotJson());
            qvo.setSnapshotHash(pq.getSnapshotHash());
            qvos.add(qvo);
        }
        vo.setQuestions(qvos);
        return vo;
    }

    @Override
    @Transactional
    public Long addQuestion(Long paperId, PaperAddQuestionRequest request, Long actorId, boolean isAdmin) {
        loadPaperForManage(paperId, actorId, isAdmin);

        QbQuestion q = questionMapper.selectById(request.getQuestionId());
        if (q == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "question not found");
        }
        if (!isAdmin && !canTeacherUseQuestion(q, actorId)) {
            throw BizException.of(ErrorCode.FORBIDDEN, "no permission to use this question");
        }

        String snapshotJson = buildQuestionSnapshot(q.getId());
        String snapshotHash = HashUtil.sha256(snapshotJson);

        QbPaperQuestion pq = new QbPaperQuestion();
        pq.setPaperId(paperId);
        pq.setQuestionId(request.getQuestionId());
        pq.setOrderNo(request.getOrderNo());
        pq.setScore(request.getScore());
        pq.setSnapshotJson(snapshotJson);
        pq.setSnapshotHash(snapshotHash);
        paperQuestionMapper.insert(pq);

        recalculateTotalScore(paperId, actorId, isAdmin);
        return pq.getId();
    }

    @Override
    public void updatePaperQuestion(Long paperQuestionId, PaperQuestionUpdateRequest request, Long actorId, boolean isAdmin) {
        QbPaperQuestion pq = paperQuestionMapper.selectById(paperQuestionId);
        if (pq == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "paper question not found");
        }
        loadPaperForManage(pq.getPaperId(), actorId, isAdmin);

        pq.setOrderNo(request.getOrderNo());
        pq.setScore(request.getScore());
        paperQuestionMapper.update(pq);
        recalculateTotalScore(pq.getPaperId(), actorId, isAdmin);
    }

    @Override
    public void removePaperQuestion(Long paperQuestionId, Long actorId, boolean isAdmin) {
        QbPaperQuestion pq = paperQuestionMapper.selectById(paperQuestionId);
        if (pq == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "paper question not found");
        }
        loadPaperForManage(pq.getPaperId(), actorId, isAdmin);

        paperQuestionMapper.deleteById(paperQuestionId);
        recalculateTotalScore(pq.getPaperId(), actorId, isAdmin);
    }

    @Override
    public void recalculateTotalScore(Long paperId, Long actorId, boolean isAdmin) {
        QbPaper p = loadPaperForManage(paperId, actorId, isAdmin);
        int total = paperQuestionMapper.sumScoreByPaperId(paperId);
        p.setTotalScore(total);
        paperMapper.update(p);
    }

    private QbPaper loadPaperForManage(Long paperId, Long actorId, boolean isAdmin) {
        QbPaper p = paperMapper.selectById(paperId);
        if (p == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "paper not found");
        }
        if (!isAdmin && !Objects.equals(p.getCreatorId(), actorId)) {
            throw BizException.of(ErrorCode.FORBIDDEN, "no permission to manage this paper");
        }
        return p;
    }

    private boolean canTeacherUseQuestion(QbQuestion q, Long teacherId) {
        if (Objects.equals(q.getCreatedBy(), teacherId)) {
            return true;
        }
        if (q.getCreatedBy() == null || q.getStatus() == null || q.getStatus() != 2) {
            return false;
        }
        List<String> roles = roleMapper.selectRoleCodesByUserId(q.getCreatedBy());
        return roles.stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r));
    }

    private String buildQuestionSnapshot(Long questionId) {
        try {
            QbQuestion q = questionMapper.selectById(questionId);
            List<QbQuestionOption> opts = optionMapper.selectByQuestionId(questionId);
            List<QbQuestionCase> cases = caseMapper.selectByQuestionId(questionId);
            List<Long> tagIds = questionTagMapper.selectTagIdsByQuestionId(questionId);

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", q.getId());
            m.put("title", q.getTitle());
            m.put("questionType", q.getQuestionType());
            m.put("difficulty", q.getDifficulty());
            m.put("chapter", q.getChapter());
            m.put("stem", q.getStem());
            m.put("standardAnswer", q.getStandardAnswer());
            m.put("answerFormat", q.getAnswerFormat());
            m.put("analysisText", q.getAnalysisText());
            m.put("analysisSource", q.getAnalysisSource());
            m.put("tagIds", tagIds);
            m.put("options", opts);
            m.put("cases", cases);
            return objectMapper.writeValueAsString(m);
        } catch (Exception e) {
            throw new BizException(ErrorCode.BIZ_ERROR, "failed to build question snapshot: " + e.getMessage());
        }
    }
}