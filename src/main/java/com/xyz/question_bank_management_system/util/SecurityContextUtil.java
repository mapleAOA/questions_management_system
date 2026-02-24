package com.xyz.question_bank_management_system.util;

import com.xyz.question_bank_management_system.exception.BizException;
import com.xyz.question_bank_management_system.exception.ErrorCode;
import com.xyz.question_bank_management_system.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

public final class SecurityContextUtil {

    private SecurityContextUtil() {
    }

    public static Long currentUserId() {
        UserPrincipal p = currentPrincipal();
        return p == null ? null : p.getUserId();
    }

    public static String currentUsername() {
        UserPrincipal p = currentPrincipal();
        return p == null ? null : p.getUsername();
    }

    public static List<String> currentRoles() {
        UserPrincipal p = currentPrincipal();
        return p == null ? Collections.emptyList() : p.getRoleCodes();
    }

    public static UserPrincipal currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return (UserPrincipal) principal;
        }
        return null;
    }

    public static Long getUserId() {
        Long userId = currentUserId();
        if (userId == null) {
            throw BizException.of(ErrorCode.UNAUTHORIZED, "未登录");
        }
        return userId;
    }
}
