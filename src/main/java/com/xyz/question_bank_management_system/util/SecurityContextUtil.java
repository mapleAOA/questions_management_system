package com.xyz.question_bank_management_system.util;

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
        //LC待写
        return null;
    }
}
