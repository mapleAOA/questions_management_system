package com.xyz.question_bank_management_system.service;

import com.xyz.question_bank_management_system.dto.*;
import com.xyz.question_bank_management_system.vo.UserListItemVO;
import com.xyz.question_bank_management_system.common.PageResponse;

public interface AuthService {

    LoginResponse register(RegisterRequest request, String ip, String userAgent);

    LoginResponse login(LoginRequest request, String ip, String userAgent);

    LoginResponse.UserDTO me();

    PageResponse<UserListItemVO> pageUsers(long page, long size);

    Long createUser(AdminCreateUserRequest request);

    void updateUser(Long userId, AdminUpdateUserRequest request);

    void updateUserRoles(Long userId, AdminUpdateUserRolesRequest request);

    Long adminCreateUser(AdminCreateUserRequest request);

    void adminUpdateUser(Long userId, AdminUpdateUserRequest request);

    void adminUpdateUserRoles(Long userId, AdminUpdateUserRolesRequest request);
}
