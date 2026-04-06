package com.xyz.question_bank_management_system.util;

import com.xyz.question_bank_management_system.exception.BizException;
import com.xyz.question_bank_management_system.exception.ErrorCode;
import com.xyz.question_bank_management_system.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityContextUtilTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUserId_shouldThrowUnauthorizedWhenNoAuthentication() {
        BizException ex = assertThrows(BizException.class, SecurityContextUtil::getUserId);
        assertEquals(ErrorCode.UNAUTHORIZED, ex.getCode());
    }

    @Test
    void getUserId_shouldReturnCurrentUserId() {
        UserPrincipal principal = new UserPrincipal(
                1001L,
                "student01",
                "pwd",
                true,
                "STUDENT"
        );
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertEquals(1001L, SecurityContextUtil.getUserId());
        assertEquals("student01", SecurityContextUtil.currentUsername());
    }
}
