package com.xyz.question_bank_management_system.service.impl;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.dto.*;
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
import com.xyz.question_bank_management_system.util.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    public LoginResponse login(LoginRequest request, String ip, String userAgent) {
        SysUser user = sysUserMapper.selectByUsername(request.getUsername());
        if (user == null) {
            writeLoginLog(null, request.getUsername(), 0, "用户不存在", ip, userAgent);
            return new LoginResponse(false, null, null, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            writeLoginLog(user.getId(), user.getUsername(), 0, "账号已禁用", ip, userAgent);
            return new LoginResponse(false, null, null, "账号已禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            writeLoginLog(user.getId(), user.getUsername(), 0, "密码错误", ip, userAgent);
            return new LoginResponse(false, null, null, "用户名或密码错误");
        }

        List<String> roles = sysRoleMapper.selectRoleCodesByUserId(user.getId());
        String rolesCsv = String.join(",", roles);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), rolesCsv);

        sysUserMapper.updateLastLoginAt(user.getId(), LocalDateTime.now());
        writeLoginLog(user.getId(), user.getUsername(), 1, null, ip, userAgent);

        LoginResponse.UserDTO userDTO = new LoginResponse.UserDTO(user.getId(), user.getUsername(), user.getDisplayName(), user.getEmail(), roles);
        return new LoginResponse(true, token, userDTO, "ok");
    }

    @Override
    public LoginResponse.UserDTO me() {
        Long uid = SecurityContextUtil.currentUserId();
        if (uid == null) {
            throw BizException.of(ErrorCode.UNAUTHORIZED, "未登录");
        }
        SysUser user = sysUserMapper.selectById(uid);
        if (user == null) {
            throw BizException.of(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        List<String> roles = sysRoleMapper.selectRoleCodesByUserId(uid);
        return new LoginResponse.UserDTO(user.getId(), user.getUsername(), user.getDisplayName(), user.getEmail(), roles);
    }

    @Override
    public PageResponse<com.xyz.question_bank_management_system.vo.UserListItemVO> pageUsers(long page, long size) {
        long offset = (page - 1) * size;
        List<SysUser> users = sysUserMapper.page(offset, size);
        long total = sysUserMapper.countAll();

        List<com.xyz.question_bank_management_system.vo.UserListItemVO> list = new ArrayList<>();
        for (SysUser u : users) {
            com.xyz.question_bank_management_system.vo.UserListItemVO vo = new com.xyz.question_bank_management_system.vo.UserListItemVO();
            vo.setId(u.getId());
            vo.setUsername(u.getUsername());
            vo.setDisplayName(u.getDisplayName());
            vo.setEmail(u.getEmail());
            vo.setStatus(u.getStatus());
            vo.setCreatedAt(u.getCreatedAt());
            vo.setRoles(sysRoleMapper.selectRoleCodesByUserId(u.getId()));
            list.add(vo);
        }
        return PageResponse.of(page, size, total, list);
    }

    @Override
    @Transactional
    public Long createUser(AdminCreateUserRequest request) {
        SysUser existed = sysUserMapper.selectByUsername(request.getUsername());
        if (existed != null) {
            throw BizException.of(ErrorCode.CONFLICT, "username已存在");
        }
        SysUser u = new SysUser();
        u.setUsername(request.getUsername());
        u.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        u.setDisplayName(request.getDisplayName());
        u.setEmail(request.getEmail());
        u.setStatus(request.getStatus());
        sysUserMapper.insert(u);

        bindRoles(u.getId(), request.getRoles());
        return u.getId();
    }

    @Override
    public void updateUser(Long userId, AdminUpdateUserRequest request) {
        SysUser u = sysUserMapper.selectById(userId);
        if (u == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (request.getDisplayName() != null) u.setDisplayName(request.getDisplayName());
        if (request.getEmail() != null) u.setEmail(request.getEmail());
        if (request.getStatus() != null) u.setStatus(request.getStatus());
        sysUserMapper.update(u);
    }

    @Override
    @Transactional
    public void updateUserRoles(Long userId, AdminUpdateUserRolesRequest request) {
        SysUser u = sysUserMapper.selectById(userId);
        if (u == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "用户不存在");
        }
        sysUserRoleMapper.deleteByUserId(userId);
        bindRoles(userId, request.getRoles());
    }

    @Override
    public Long adminCreateUser(AdminCreateUserRequest request) {
        //LC待写
        return null;
    }

    @Override
    public void adminUpdateUser(Long userId, AdminUpdateUserRequest request) {
        //LC待写
    }

    @Override
    public void adminUpdateUserRoles(Long userId, AdminUpdateUserRolesRequest request) {
        //LC待写
    }

    private void bindRoles(Long userId, List<String> roleCodes) {
        if (roleCodes == null) return;
        for (String rc : roleCodes) {
            if (rc == null || rc.isBlank()) continue;
            SysRole role = sysRoleMapper.selectByCode(rc);
            if (role == null) {
                // 方便初始化：若数据库未预置角色，则自动创建
                SysRole r = new SysRole();
                r.setRoleCode(rc);
                r.setRoleName(rc);
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
            // 登录日志失败不影响主流程
        }
    }
}
