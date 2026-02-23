package com.xyz.question_bank_management_system.controller;

import com.xyz.question_bank_management_system.common.ApiResponse;
import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.dto.AdminCreateUserRequest;
import com.xyz.question_bank_management_system.dto.AdminUpdateUserRequest;
import com.xyz.question_bank_management_system.dto.AdminUpdateUserRolesRequest;
import com.xyz.question_bank_management_system.service.AuthService;
import com.xyz.question_bank_management_system.vo.UserListItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AuthService authService;

    @GetMapping
    public ApiResponse<PageResponse<UserListItemVO>> page(@RequestParam(defaultValue = "1") long page,
                                                         @RequestParam(defaultValue = "20") long size) {
        return ApiResponse.ok(authService.pageUsers(page, size));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody @Valid AdminCreateUserRequest request) {
        return ApiResponse.ok(authService.adminCreateUser(request));
    }

    @PutMapping("/{userId}")
    public ApiResponse<Void> update(@PathVariable Long userId, @RequestBody @Valid AdminUpdateUserRequest request) {
        authService.adminUpdateUser(userId, request);
        return ApiResponse.ok();
    }

    @PutMapping("/{userId}/roles")
    public ApiResponse<Void> updateRoles(@PathVariable Long userId, @RequestBody @Valid AdminUpdateUserRolesRequest request) {
        authService.adminUpdateUserRoles(userId, request);
        return ApiResponse.ok();
    }
}
