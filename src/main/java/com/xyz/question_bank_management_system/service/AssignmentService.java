package com.xyz.question_bank_management_system.service;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.dto.AssignmentTargetsRequest;
import com.xyz.question_bank_management_system.dto.AssignmentUpsertRequest;
import com.xyz.question_bank_management_system.entity.QbAssignment;

public interface AssignmentService {

    Long create(AssignmentUpsertRequest request, Long creatorId);

    void update(Long assignmentId, AssignmentUpsertRequest request);

    void delete(Long assignmentId);

    void publish(Long assignmentId);

    void close(Long assignmentId);

    void setTargets(Long assignmentId, AssignmentTargetsRequest request);

    /**
     * teacherId: 教师本人（教师端列表）；isAdmin=true 时忽略 teacherId 返回全量。
     */
    PageResponse<QbAssignment> pageMineOrAll(long page, long size, Long teacherId, boolean isAdmin);

    QbAssignment detail(Long assignmentId);
}
