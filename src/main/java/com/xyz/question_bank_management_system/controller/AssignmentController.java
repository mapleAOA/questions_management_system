package com.xyz.question_bank_management_system.controller;

import com.xyz.question_bank_management_system.common.ApiResponse;
import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.dto.AssignmentTargetsRequest;
import com.xyz.question_bank_management_system.dto.AssignmentUpsertRequest;
import com.xyz.question_bank_management_system.entity.QbAssignment;
import com.xyz.question_bank_management_system.service.AssignmentService;
import com.xyz.question_bank_management_system.util.SecurityContextUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Stream;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping
    public ApiResponse<Long> create(@RequestBody @Valid AssignmentUpsertRequest request) {
        Long uid = SecurityContextUtil.getUserId();
        return ApiResponse.ok(assignmentService.create(request, uid));
    }

    @PutMapping("/{assignmentId}")
    public ApiResponse<Void> update(@PathVariable Long assignmentId, @RequestBody @Valid AssignmentUpsertRequest request) {
        assignmentService.update(assignmentId, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{assignmentId}")
    public ApiResponse<Void> delete(@PathVariable Long assignmentId) {
        assignmentService.delete(assignmentId);
        return ApiResponse.ok();
    }

    @GetMapping
    public ApiResponse<PageResponse<QbAssignment>> page(@RequestParam(defaultValue = "1") long page,
                                                       @RequestParam(defaultValue = "20") long size) {
        Long uid = SecurityContextUtil.getUserId();
        boolean isAdmin = hasRole("ROLE_ADMIN");
        return ApiResponse.ok(assignmentService.pageMineOrAll(page, size, uid, isAdmin));
    }

    @GetMapping("/{assignmentId}")
    public ApiResponse<QbAssignment> detail(@PathVariable Long assignmentId) {
        return ApiResponse.ok(assignmentService.detail(assignmentId));
    }

    @PostMapping("/{assignmentId}/publish")
    public ApiResponse<Void> publish(@PathVariable Long assignmentId) {
        assignmentService.publish(assignmentId);
        return ApiResponse.ok();
    }

    @PostMapping("/{assignmentId}/close")
    public ApiResponse<Void> close(@PathVariable Long assignmentId) {
        assignmentService.close(assignmentId);
        return ApiResponse.ok();
    }

    @PutMapping("/{assignmentId}/targets")
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
