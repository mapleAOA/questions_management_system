package com.xyz.question_bank_management_system.service.impl;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.common.enums.AssignmentPublishStatusEnum;
import com.xyz.question_bank_management_system.dto.AssignmentTargetsRequest;
import com.xyz.question_bank_management_system.dto.AssignmentUpsertRequest;
import com.xyz.question_bank_management_system.entity.QbAssignment;
import com.xyz.question_bank_management_system.entity.QbPaper;
import com.xyz.question_bank_management_system.exception.BizException;
import com.xyz.question_bank_management_system.exception.ErrorCode;
import com.xyz.question_bank_management_system.mapper.QbAssignmentMapper;
import com.xyz.question_bank_management_system.mapper.QbAssignmentTargetClassMapper;
import com.xyz.question_bank_management_system.mapper.QbAssignmentTargetMapper;
import com.xyz.question_bank_management_system.mapper.QbPaperMapper;
import com.xyz.question_bank_management_system.service.AssignmentService;
import com.xyz.question_bank_management_system.util.PageParamUtil;
import com.xyz.question_bank_management_system.vo.AssignmentMyItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final QbAssignmentMapper assignmentMapper;
    private final QbAssignmentTargetMapper targetMapper;
    private final QbAssignmentTargetClassMapper targetClassMapper;
    private final QbPaperMapper paperMapper;

    @Override
    @Transactional
    public Long create(AssignmentUpsertRequest request, Long creatorId) {
        ensurePaperExists(request.getPaperId());
        validateTimeRange(request);

        QbAssignment a = new QbAssignment();
        a.setPaperId(request.getPaperId());
        a.setAssignmentTitle(request.getAssignmentTitle());
        a.setAssignmentDesc(request.getAssignmentDesc());
        a.setStartTime(request.getStartTime());
        a.setEndTime(request.getEndTime());
        a.setTimeLimitMin(request.getTimeLimitMin());
        a.setMaxAttempts(request.getMaxAttempts());
        a.setShuffleQuestions(request.getShuffleQuestions());
        a.setShuffleOptions(request.getShuffleOptions());
        a.setPublishStatus(request.getPublishStatus());
        a.setCreatedBy(creatorId);
        assignmentMapper.insert(a);
        return a.getId();
    }

    @Override
    @Transactional
    public void update(Long assignmentId, AssignmentUpsertRequest request) {
        QbAssignment a = assignmentMapper.selectById(assignmentId);
        if (a == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "assignment not found");
        }

        ensurePaperExists(request.getPaperId());
        validateTimeRange(request);

        a.setPaperId(request.getPaperId());
        a.setAssignmentTitle(request.getAssignmentTitle());
        a.setAssignmentDesc(request.getAssignmentDesc());
        a.setStartTime(request.getStartTime());
        a.setEndTime(request.getEndTime());
        a.setTimeLimitMin(request.getTimeLimitMin());
        a.setMaxAttempts(request.getMaxAttempts());
        a.setShuffleQuestions(request.getShuffleQuestions());
        a.setShuffleOptions(request.getShuffleOptions());
        a.setPublishStatus(request.getPublishStatus());
        assignmentMapper.update(a);
    }

    @Override
    @Transactional
    public void delete(Long assignmentId) {
        QbAssignment a = assignmentMapper.selectById(assignmentId);
        if (a == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "assignment not found");
        }
        assignmentMapper.softDelete(assignmentId);
        targetMapper.deleteByAssignmentId(assignmentId);
        targetClassMapper.deleteByAssignmentId(assignmentId);
    }

    @Override
    public void publish(Long assignmentId) {
        QbAssignment a = assignmentMapper.selectById(assignmentId);
        if (a == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "assignment not found");
        }
        assignmentMapper.updatePublishStatus(assignmentId, AssignmentPublishStatusEnum.PUBLISHED.getCode());
    }

    @Override
    public void close(Long assignmentId) {
        QbAssignment a = assignmentMapper.selectById(assignmentId);
        if (a == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "assignment not found");
        }
        assignmentMapper.updatePublishStatus(assignmentId, AssignmentPublishStatusEnum.CLOSED.getCode());
    }

    @Override
    @Transactional
    public void setTargets(Long assignmentId, AssignmentTargetsRequest request) {
        QbAssignment a = assignmentMapper.selectById(assignmentId);
        if (a == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "assignment not found");
        }

        List<Long> userIds = request.getUserIds() == null ? List.of() : request.getUserIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<Long> classIds = request.getClassIds() == null ? List.of() : request.getClassIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        targetMapper.deleteByAssignmentId(assignmentId);
        targetClassMapper.deleteByAssignmentId(assignmentId);
        if (!userIds.isEmpty()) {
            targetMapper.batchInsert(assignmentId, userIds);
        }
        if (!classIds.isEmpty()) {
            targetClassMapper.batchInsert(assignmentId, classIds);
        }
    }

    @Override
    public PageResponse<QbAssignment> pageMineOrAll(long page, long size, Long teacherId, boolean isAdmin) {
        long safePage = PageParamUtil.normalizePage(page);
        long safeSize = PageParamUtil.normalizeSize(size);
        long offset = PageParamUtil.offset(safePage, safeSize);

        if (isAdmin) {
            List<QbAssignment> rows = assignmentMapper.pageAll(offset, safeSize);
            long total = assignmentMapper.countAll();
            return PageResponse.of(safePage, safeSize, total, rows);
        }
        List<QbAssignment> rows = assignmentMapper.pageByTeacher(teacherId, offset, safeSize);
        long total = assignmentMapper.countByTeacher(teacherId);
        return PageResponse.of(safePage, safeSize, total, rows);
    }

    @Override
    public QbAssignment detail(Long assignmentId) {
        QbAssignment a = assignmentMapper.selectById(assignmentId);
        if (a == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "assignment not found");
        }
        return a;
    }

    @Override
    public QbAssignment detailForStudent(Long assignmentId, Long userId) {
        QbAssignment a = assignmentMapper.selectById(assignmentId);
        if (a == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "assignment not found");
        }
        if (a.getPublishStatus() == null || a.getPublishStatus() == AssignmentPublishStatusEnum.DRAFT.getCode()) {
            throw BizException.of(ErrorCode.FORBIDDEN, "assignment is not available");
        }

        long userTargetCount = targetMapper.countByAssignmentId(assignmentId);
        long classTargetCount = targetClassMapper.countByAssignmentId(assignmentId);
        boolean hasAnyTarget = userTargetCount > 0 || classTargetCount > 0;
        if (hasAnyTarget
                && targetMapper.countByAssignmentAndUser(assignmentId, userId) <= 0
                && targetClassMapper.countByAssignmentAndStudent(assignmentId, userId) <= 0) {
            throw BizException.of(ErrorCode.FORBIDDEN, "you are not in assignment targets");
        }
        return a;
    }

    @Override
    public PageResponse<AssignmentMyItemVO> pageForStudent(String status, long page, long size, Long userId) {
        String safeStatus = normalizeStudentStatus(status);
        long safePage = PageParamUtil.normalizePage(page);
        long safeSize = PageParamUtil.normalizeSize(size);
        long offset = PageParamUtil.offset(safePage, safeSize);
        LocalDateTime now = LocalDateTime.now();

        List<AssignmentMyItemVO> rows = assignmentMapper.pageForStudent(userId, safeStatus, now, offset, safeSize);
        long total = assignmentMapper.countForStudent(userId, safeStatus, now);
        return PageResponse.of(safePage, safeSize, total, rows);
    }

    private void ensurePaperExists(Long paperId) {
        QbPaper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "paper not found");
        }
    }

    private void validateTimeRange(AssignmentUpsertRequest request) {
        if (request.getEndTime() == null) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "endTime cannot be null");
        }
        if (request.getStartTime() != null && request.getEndTime() != null
                && request.getEndTime().isBefore(request.getStartTime())) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "endTime must be greater than or equal to startTime");
        }
    }

    private String normalizeStudentStatus(String status) {
        if (status == null || status.isBlank()) {
            return "all";
        }
        String normalized = status.trim().toLowerCase();
        if ("ongoing".equals(normalized) || "expired".equals(normalized) || "all".equals(normalized)) {
            return normalized;
        }
        throw BizException.of(ErrorCode.PARAM_ERROR, "status must be one of: ongoing, expired, all");
    }
}
