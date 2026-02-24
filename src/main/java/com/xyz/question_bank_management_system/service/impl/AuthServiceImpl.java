package com.xyz.question_bank_management_system.service.impl;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.common.enums.RoleCode;
import com.xyz.question_bank_management_system.dto.AdminCreateUserRequest;
import com.xyz.question_bank_management_system.dto.AdminUpdateUserRequest;
import com.xyz.question_bank_management_system.dto.AdminUpdateUserRolesRequest;
import com.xyz.question_bank_management_system.dto.LoginRequest;
import com.xyz.question_bank_management_system.dto.LoginResponse;
import com.xyz.question_bank_management_system.dto.RegisterRequest;
import com.xyz.question_bank_management_system.entity.SysLoginLog;
import com.xyz.question_bank_management_system.entity.SysRole;
import com.xyz.question_bank_management_system.entity.SysUser;
import com.xyz.question_bank_management_system.exception.BizException;
import com.xyz.question_bank_management_system.exception.ErrorCode;
import com.xyz.question_bank_management_system.mapper.SysLoginLogMapper;
import com.xyz.question_bank_management_system.mapper.SysRoleMapper;
import com.xyz.question_bank_management_system.mapper.SysUserMapper;
import com.xyz.question_bank_management_system.mapper.SysUserRoleMapper;
import com.xyz.question_bank_management_system.security.JwtUtil;
import com.xyz.question_bank_management_system.service.AuthService;
import com.xyz.question_bank_management_system.util.PageParamUtil;
import com.xyz.question_bank_management_system.util.SecurityContextUtil;
import com.xyz.question_bank_management_system.vo.UserListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysLoginLogMapper sysLoginLogMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request, String ip, String userAgent) {
        SysUser existed = sysUserMapper.selectByUsername(request.getUsername());
        if (existed != null) {
            return new LoginResponse(false, null, null, "username already exists");
        }

        String roleCode = normalizeRegisterRole(request.getRole());

        SysUser u = new SysUser();
        u.setUsername(request.getUsername());
        u.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        String displayName = request.getDisplayName();
        if (displayName == null || displayName.isBlank()) {
            displayName = request.getUsername();
        }
        u.setDisplayName(displayName);
        u.setEmail(request.getEmail());
        u.setStatus(1);
        u.setIsDeleted(0);
        sysUserMapper.insert(u);

        bindRoles(u.getId(), List.of(roleCode));

        List<String> roles = sysRoleMapper.selectRoleCodesByUserId(u.getId());
        String token = jwtUtil.generateToken(u.getId(), u.getUsername(), String.join(",", roles));
        sysUserMapper.updateLastLoginAt(u.getId(), LocalDateTime.now());
        writeLoginLog(u.getId(), u.getUsername(), 1, null, ip, userAgent);

        LoginResponse.UserDTO userDTO = new LoginResponse.UserDTO(
                u.getId(),
                u.getUsername(),
                u.getDisplayName(),
                u.getEmail(),
                roles
        );
        return new LoginResponse(true, token, userDTO, "ok");
    }

    @Override
    public LoginResponse login(LoginRequest request, String ip, String userAgent) {
        SysUser user = sysUserMapper.selectByUsername(request.getUsername());
        if (user == null) {
            writeLoginLog(null, request.getUsername(), 0, "user not found", ip, userAgent);
            return new LoginResponse(false, null, null, "invalid username or password");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            writeLoginLog(user.getId(), user.getUsername(), 0, "account disabled", ip, userAgent);
            return new LoginResponse(false, null, null, "account disabled");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            writeLoginLog(user.getId(), user.getUsername(), 0, "wrong password", ip, userAgent);
            return new LoginResponse(false, null, null, "invalid username or password");
        }

        List<String> roles = sysRoleMapper.selectRoleCodesByUserId(user.getId());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), String.join(",", roles));

        sysUserMapper.updateLastLoginAt(user.getId(), LocalDateTime.now());
        writeLoginLog(user.getId(), user.getUsername(), 1, null, ip, userAgent);

        LoginResponse.UserDTO userDTO = new LoginResponse.UserDTO(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                roles
        );
        return new LoginResponse(true, token, userDTO, "ok");
    }

    @Override
    public LoginResponse.UserDTO me() {
        Long uid = SecurityContextUtil.currentUserId();
        if (uid == null) {
            throw BizException.of(ErrorCode.UNAUTHORIZED, "unauthorized");
        }

        SysUser user = sysUserMapper.selectById(uid);
        if (user == null) {
            throw BizException.of(ErrorCode.UNAUTHORIZED, "user not found");
        }

        List<String> roles = sysRoleMapper.selectRoleCodesByUserId(uid);
        return new LoginResponse.UserDTO(user.getId(), user.getUsername(), user.getDisplayName(), user.getEmail(), roles);
    }

    @Override
    public PageResponse<UserListItemVO> pageUsers(long page, long size) {
        long safePage = PageParamUtil.normalizePage(page);
        long safeSize = PageParamUtil.normalizeSize(size);
        long offset = PageParamUtil.offset(safePage, safeSize);

        List<SysUser> users = sysUserMapper.page(offset, safeSize);
        long total = sysUserMapper.countAll();

        List<UserListItemVO> list = new ArrayList<>();
        for (SysUser u : users) {
            UserListItemVO vo = new UserListItemVO();
            vo.setId(u.getId());
            vo.setUsername(u.getUsername());
            vo.setDisplayName(u.getDisplayName());
            vo.setEmail(u.getEmail());
            vo.setStatus(u.getStatus());
            vo.setCreatedAt(u.getCreatedAt());
            vo.setRoles(sysRoleMapper.selectRoleCodesByUserId(u.getId()));
            list.add(vo);
        }
        return PageResponse.of(safePage, safeSize, total, list);
    }

    @Override
    @Transactional
    public Long createUser(AdminCreateUserRequest request) {
        SysUser existed = sysUserMapper.selectByUsername(request.getUsername());
        if (existed != null) {
            throw BizException.of(ErrorCode.CONFLICT, "username already exists");
        }

        SysUser u = new SysUser();
        u.setUsername(request.getUsername());
        u.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        u.setDisplayName(request.getDisplayName());
        u.setEmail(request.getEmail());
        u.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        u.setIsDeleted(0);
        sysUserMapper.insert(u);

        bindRoles(u.getId(), request.getRoles());
        return u.getId();
    }

    @Override
    public void updateUser(Long userId, AdminUpdateUserRequest request) {
        SysUser u = sysUserMapper.selectById(userId);
        if (u == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "user not found");
        }

        if (request.getDisplayName() != null) {
            u.setDisplayName(request.getDisplayName());
        }
        if (request.getEmail() != null) {
            u.setEmail(request.getEmail());
        }
        if (request.getStatus() != null) {
            u.setStatus(request.getStatus());
        }
        sysUserMapper.update(u);
    }

    @Override
    @Transactional
    public void updateUserRoles(Long userId, AdminUpdateUserRolesRequest request) {
        SysUser u = sysUserMapper.selectById(userId);
        if (u == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "user not found");
        }
        sysUserRoleMapper.deleteByUserId(userId);
        bindRoles(userId, request.getRoles());
    }

    @Override
    public Long adminCreateUser(AdminCreateUserRequest request) {
        return createUser(request);
    }

    @Override
    public void adminUpdateUser(Long userId, AdminUpdateUserRequest request) {
        updateUser(userId, request);
    }

    @Override
    public void adminUpdateUserRoles(Long userId, AdminUpdateUserRolesRequest request) {
        updateUserRoles(userId, request);
    }

    private void bindRoles(Long userId, List<String> roleCodes) {
        if (roleCodes == null) {
            return;
        }

        LinkedHashSet<String> uniqueCodes = new LinkedHashSet<>();
        for (String rc : roleCodes) {
            if (rc == null) {
                continue;
            }
            String normalized = rc.trim();
            if (normalized.isEmpty()) {
                continue;
            }
            uniqueCodes.add(normalized.toUpperCase(Locale.ROOT));
        }

        for (String code : uniqueCodes) {
            if (code.isBlank()) {
                continue;
            }

            SysRole role = sysRoleMapper.selectByCode(code);
            if (role == null) {
                SysRole r = new SysRole();
                r.setRoleCode(code);
                r.setRoleName(code);
                sysRoleMapper.insert(r);
                role = r;
            }
            sysUserRoleMapper.insert(userId, role.getId());
        }
    }

    private void writeLoginLog(Long userId, String username, int successFlag, String failReason, String ip, String userAgent) {
        try {
            SysLoginLog log = new SysLoginLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setSuccessFlag(successFlag);
            log.setFailReason(failReason);
            log.setIpAddr(ip);
            log.setUserAgent(userAgent);
            sysLoginLogMapper.insert(log);
        } catch (Exception ignore) {
            // ignore login log errors
        }
    }

    private String normalizeRegisterRole(String role) {
        if (role == null) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "role is required");
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        if (RoleCode.STUDENT.name().equals(normalized) || RoleCode.TEACHER.name().equals(normalized)) {
            return normalized;
        }
        throw BizException.of(ErrorCode.PARAM_ERROR, "role must be STUDENT or TEACHER");
    }
}
