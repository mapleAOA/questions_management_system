package com.xyz.question_bank_management_system.controller;

import com.xyz.question_bank_management_system.common.ApiResponse;
import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.dto.AssignmentTargetsRequest;
import com.xyz.question_bank_management_system.dto.AssignmentUpsertRequest;
import com.xyz.question_bank_management_system.entity.QbAssignment;
import com.xyz.question_bank_management_system.service.AssignmentService;
import com.xyz.question_bank_management_system.util.SecurityContextUtil;
import com.xyz.question_bank_management_system.vo.AssignmentMyItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Long> create(@RequestBody @Valid AssignmentUpsertRequest request) {
        Long uid = SecurityContextUtil.getUserId();
        return ApiResponse.ok(assignmentService.create(request, uid));
    }

    @PutMapping("/{assignmentId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Void> update(@PathVariable Long assignmentId, @RequestBody @Valid AssignmentUpsertRequest request) {
        assignmentService.update(assignmentId, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long assignmentId) {
        assignmentService.delete(assignmentId);
        return ApiResponse.ok();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<PageResponse<QbAssignment>> page(@RequestParam(defaultValue = "1") long page,
                                                       @RequestParam(defaultValue = "20") long size) {
        Long uid = SecurityContextUtil.getUserId();
        boolean isAdmin = hasRole("ROLE_ADMIN");
        return ApiResponse.ok(assignmentService.pageMineOrAll(page, size, uid, isAdmin));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<PageResponse<AssignmentMyItemVO>> my(@RequestParam(required = false) String status,
                                                             @RequestParam(defaultValue = "1") long page,
                                                             @RequestParam(defaultValue = "10") long size) {
        Long uid = SecurityContextUtil.getUserId();
        return ApiResponse.ok(assignmentService.pageForStudent(status, page, size, uid));
    }

    @GetMapping("/{assignmentId}")
    public ApiResponse<QbAssignment> detail(@PathVariable Long assignmentId) {
        Long uid = SecurityContextUtil.getUserId();
        if (hasRole("ROLE_ADMIN") || hasRole("ROLE_TEACHER")) {
            return ApiResponse.ok(assignmentService.detail(assignmentId));
        }
        return ApiResponse.ok(assignmentService.detailForStudent(assignmentId, uid));
    }

    @PostMapping("/{assignmentId}/publish")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Void> publish(@PathVariable Long assignmentId) {
        assignmentService.publish(assignmentId);
        return ApiResponse.ok();
    }

    @PostMapping("/{assignmentId}/close")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Void> close(@PathVariable Long assignmentId) {
        assignmentService.close(assignmentId);
        return ApiResponse.ok();
    }

    @PutMapping("/{assignmentId}/targets")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Void> setTargets(@PathVariable Long assignmentId, @RequestBody @Valid AssignmentTargetsRequest request) {
        assignmentService.setTargets(assignmentId, request);
        return ApiResponse.ok();
    }

    private boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(role));
    }
}
