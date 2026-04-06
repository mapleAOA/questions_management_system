package com.xyz.question_bank_management_system.service;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.dto.AdminCreateUserRequest;
import com.xyz.question_bank_management_system.dto.AdminUpdateUserRequest;
import com.xyz.question_bank_management_system.dto.AdminUpdateUserRoleRequest;
import com.xyz.question_bank_management_system.dto.LoginRequest;
import com.xyz.question_bank_management_system.dto.LoginResponse;
import com.xyz.question_bank_management_system.dto.RegisterRequest;
import com.xyz.question_bank_management_system.vo.UserListItemVO;

public interface AuthService {

    LoginResponse register(RegisterRequest request, String ip, String userAgent);

    LoginResponse login(LoginRequest request, String ip, String userAgent);

    LoginResponse.UserDTO me();

    PageResponse<UserListItemVO> pageUsers(long page, long size);

    Long createUser(AdminCreateUserRequest request);

    void updateUser(Long userId, AdminUpdateUserRequest request);

    void updateUserRole(Long userId, AdminUpdateUserRoleRequest request);

    Long adminCreateUser(AdminCreateUserRequest request);

    void adminUpdateUser(Long userId, AdminUpdateUserRequest request);

    void adminUpdateUserRole(Long userId, AdminUpdateUserRoleRequest request);
}
