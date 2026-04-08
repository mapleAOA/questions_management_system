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
import com.xyz.question_bank_management_system.service.AuditLogService;
import com.xyz.question_bank_management_system.service.AssignmentService;
import com.xyz.question_bank_management_system.util.PageParamUtil;
import com.xyz.question_bank_management_system.vo.AssignmentMyItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final QbAssignmentMapper assignmentMapper;
    private final QbAssignmentTargetMapper targetMapper;
    private final QbAssignmentTargetClassMapper targetClassMapper;
    private final QbPaperMapper paperMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public Long create(AssignmentUpsertRequest request, Long creatorId, boolean isAdmin) {
        ensurePaperUsable(request.getPaperId(), creatorId, isAdmin);
        validateTimeRange(request);

        QbAssignment a = new QbAssignment();
        a.setPaperId(request.getPaperId());
        a.setAssignmentTitle(request.getAssignmentTitle());
        a.setAssignmentDesc(request.getAssignmentDesc());
        a.setStartTime(request.getStartTime());
        a.setEndTime(request.getEndTime());
        a.setTimeLimitMin(request.getTimeLimitMin());
        a.setMaxAttempts(request.getMaxAttempts());
        a.setShuffleQuestions(0);
        a.setShuffleOptions(request.getShuffleOptions());
        a.setPublishStatus(AssignmentPublishStatusEnum.DRAFT.getCode());
        a.setCreatedBy(creatorId);
        assignmentMapper.insert(a);
        recordAudit(creatorId, "ASSIGNMENT_CREATE", "ASSIGNMENT", a.getId(), null, assignmentAuditSnapshot(a));
        return a.getId();
    }

    @Override
    @Transactional
    public void update(Long assignmentId, AssignmentUpsertRequest request, Long actorId, boolean isAdmin) {
        QbAssignment a = loadAssignmentForManage(assignmentId, actorId, isAdmin);
        Map<String, Object> before = assignmentAuditSnapshot(a);

        ensurePaperUsable(request.getPaperId(), actorId, isAdmin);
        validateTimeRange(request);

        a.setPaperId(request.getPaperId());
        a.setAssignmentTitle(request.getAssignmentTitle());
        a.setAssignmentDesc(request.getAssignmentDesc());
        a.setStartTime(request.getStartTime());
        a.setEndTime(request.getEndTime());
        a.setTimeLimitMin(request.getTimeLimitMin());
        a.setMaxAttempts(request.getMaxAttempts());
        a.setShuffleQuestions(0);
        a.setShuffleOptions(request.getShuffleOptions());
        assignmentMapper.update(a);
        recordAudit(actorId, "ASSIGNMENT_UPDATE", "ASSIGNMENT", assignmentId, before, assignmentAuditSnapshot(a));
    }

    @Override
    @Transactional
    public void delete(Long assignmentId, Long actorId, boolean isAdmin) {
        QbAssignment assignment = loadAssignmentForManage(assignmentId, actorId, isAdmin);
        assignmentMapper.softDelete(assignmentId);
        targetMapper.deleteByAssignmentId(assignmentId);
        targetClassMapper.deleteByAssignmentId(assignmentId);
        recordAudit(actorId, "ASSIGNMENT_DELETE", "ASSIGNMENT", assignmentId, assignmentAuditSnapshot(assignment), null);
    }

    @Override
    public void publish(Long assignmentId, Long actorId, boolean isAdmin) {
        QbAssignment assignment = loadAssignmentForManage(assignmentId, actorId, isAdmin);
        Map<String, Object> before = assignmentAuditSnapshot(assignment);
        assignmentMapper.updatePublishStatus(assignmentId, AssignmentPublishStatusEnum.PUBLISHED.getCode());
        assignment.setPublishStatus(AssignmentPublishStatusEnum.PUBLISHED.getCode());
        recordAudit(actorId, "ASSIGNMENT_PUBLISH", "ASSIGNMENT", assignmentId, before, assignmentAuditSnapshot(assignment));
    }

    @Override
    public void close(Long assignmentId, Long actorId, boolean isAdmin) {
        QbAssignment assignment = loadAssignmentForManage(assignmentId, actorId, isAdmin);
        Map<String, Object> before = assignmentAuditSnapshot(assignment);
        assignmentMapper.updatePublishStatus(assignmentId, AssignmentPublishStatusEnum.CLOSED.getCode());
        assignment.setPublishStatus(AssignmentPublishStatusEnum.CLOSED.getCode());
        recordAudit(actorId, "ASSIGNMENT_CLOSE", "ASSIGNMENT", assignmentId, before, assignmentAuditSnapshot(assignment));
    }

    @Override
    @Transactional
    public void setTargets(Long assignmentId, AssignmentTargetsRequest request, Long actorId, boolean isAdmin) {
        loadAssignmentForManage(assignmentId, actorId, isAdmin);

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
        Map<String, Object> after = new LinkedHashMap<>();
        after.put("userIds", userIds);
        after.put("classIds", classIds);
        recordAudit(actorId, "ASSIGNMENT_SET_TARGETS", "ASSIGNMENT", assignmentId, null, after);
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
    public QbAssignment detail(Long assignmentId, Long actorId, boolean isAdmin) {
        return loadAssignmentForManage(assignmentId, actorId, isAdmin);
    }

    @Override
    public QbAssignment detailForStudent(Long assignmentId, Long userId) {
        QbAssignment a = assignmentMapper.selectById(assignmentId);
        if (a == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "作业不存在");
        }
        if (a.getPublishStatus() == null || a.getPublishStatus() == AssignmentPublishStatusEnum.DRAFT.getCode()) {
            throw BizException.of(ErrorCode.FORBIDDEN, "该作业当前不可用");
        }

        long userTargetCount = targetMapper.countByAssignmentId(assignmentId);
        long classTargetCount = targetClassMapper.countByAssignmentId(assignmentId);
        boolean hasAnyTarget = userTargetCount > 0 || classTargetCount > 0;
        if (hasAnyTarget
                && targetMapper.countByAssignmentAndUser(assignmentId, userId) <= 0
                && targetClassMapper.countByAssignmentAndStudent(assignmentId, userId) <= 0) {
            throw BizException.of(ErrorCode.FORBIDDEN, "你不在该作业的目标名单中");
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

    private void ensurePaperUsable(Long paperId, Long actorId, boolean isAdmin) {
        QbPaper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "试卷不存在");
        }
        if (!isAdmin && !Objects.equals(paper.getCreatorId(), actorId)) {
            throw BizException.of(ErrorCode.FORBIDDEN, "无权使用该试卷");
        }
    }

    private void validateTimeRange(AssignmentUpsertRequest request) {
        if (request.getEndTime() == null) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "结束时间不能为空");
        }
        if (request.getStartTime() != null && request.getEndTime() != null
                && request.getEndTime().isBefore(request.getStartTime())) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "结束时间不能早于开始时间");
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
        throw BizException.of(ErrorCode.PARAM_ERROR, "作业状态参数不合法");
    }

    private QbAssignment loadAssignmentForManage(Long assignmentId, Long actorId, boolean isAdmin) {
        QbAssignment a = assignmentMapper.selectById(assignmentId);
        if (a == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "作业不存在");
        }
        if (!isAdmin && !Objects.equals(a.getCreatedBy(), actorId)) {
            throw BizException.of(ErrorCode.FORBIDDEN, "无权管理该作业");
        }
        return a;
    }

    private Map<String, Object> assignmentAuditSnapshot(QbAssignment assignment) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", assignment.getId());
        snapshot.put("paperId", assignment.getPaperId());
        snapshot.put("assignmentTitle", assignment.getAssignmentTitle());
        snapshot.put("assignmentDesc", assignment.getAssignmentDesc());
        snapshot.put("startTime", assignment.getStartTime());
        snapshot.put("endTime", assignment.getEndTime());
        snapshot.put("timeLimitMin", assignment.getTimeLimitMin());
        snapshot.put("maxAttempts", assignment.getMaxAttempts());
        snapshot.put("shuffleOptions", assignment.getShuffleOptions());
        snapshot.put("publishStatus", assignment.getPublishStatus());
        snapshot.put("createdBy", assignment.getCreatedBy());
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
}
