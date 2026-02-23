package com.xyz.question_bank_management_system.service.impl;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.dto.AssignmentTargetsRequest;
import com.xyz.question_bank_management_system.dto.AssignmentUpsertRequest;
import com.xyz.question_bank_management_system.entity.QbAssignment;
import com.xyz.question_bank_management_system.entity.QbPaper;
import com.xyz.question_bank_management_system.exception.BizException;
import com.xyz.question_bank_management_system.exception.ErrorCode;
import com.xyz.question_bank_management_system.mapper.QbAssignmentMapper;
import com.xyz.question_bank_management_system.mapper.QbAssignmentTargetMapper;
import com.xyz.question_bank_management_system.mapper.QbPaperMapper;
import com.xyz.question_bank_management_system.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final QbAssignmentMapper assignmentMapper;
    private final QbAssignmentTargetMapper targetMapper;
    private final QbPaperMapper paperMapper;

    @Override
    @Transactional
    public Long create(AssignmentUpsertRequest request, Long creatorId) {
        QbPaper paper = paperMapper.selectById(request.getPaperId());
        if (paper == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "试卷不存在");
        }
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
    public void update(Long assignmentId, AssignmentUpsertRequest request) {
        QbAssignment a = assignmentMapper.selectById(assignmentId);
        if (a == null) throw BizException.of(ErrorCode.NOT_FOUND, "作业不存在");
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
    public void delete(Long assignmentId) {
        QbAssignment a = assignmentMapper.selectById(assignmentId);
        if (a == null) throw BizException.of(ErrorCode.NOT_FOUND, "作业不存在");
        assignmentMapper.softDelete(assignmentId);
        targetMapper.deleteByAssignmentId(assignmentId);
    }

    @Override
    public void publish(Long assignmentId) {
        QbAssignment a = assignmentMapper.selectById(assignmentId);
        if (a == null) throw BizException.of(ErrorCode.NOT_FOUND, "作业不存在");
        assignmentMapper.updatePublishStatus(assignmentId, 2);
    }

    @Override
    public void close(Long assignmentId) {
        QbAssignment a = assignmentMapper.selectById(assignmentId);
        if (a == null) throw BizException.of(ErrorCode.NOT_FOUND, "作业不存在");
        assignmentMapper.updatePublishStatus(assignmentId, 3);
    }

    @Override
    @Transactional
    public void setTargets(Long assignmentId, AssignmentTargetsRequest request) {
        QbAssignment a = assignmentMapper.selectById(assignmentId);
        if (a == null) throw BizException.of(ErrorCode.NOT_FOUND, "作业不存在");
        targetMapper.deleteByAssignmentId(assignmentId);
        targetMapper.batchInsert(assignmentId, request.getUserIds());
    }

    @Override
    public PageResponse<QbAssignment> pageMineOrAll(long page, long size, Long teacherId, boolean isAdmin) {
        long offset = (page - 1) * size;
        if (isAdmin) {
            List<QbAssignment> rows = assignmentMapper.pageAll(offset, size);
            long total = assignmentMapper.countAll();
            return PageResponse.of(page, size, total, rows);
        }
        List<QbAssignment> rows = assignmentMapper.pageByTeacher(teacherId, offset, size);
        long total = assignmentMapper.countByTeacher(teacherId);
        return PageResponse.of(page, size, total, rows);
    }

    @Override
    public QbAssignment detail(Long assignmentId) {
        QbAssignment a = assignmentMapper.selectById(assignmentId);
        if (a == null) throw BizException.of(ErrorCode.NOT_FOUND, "作业不存在");
        return a;
    }
}
