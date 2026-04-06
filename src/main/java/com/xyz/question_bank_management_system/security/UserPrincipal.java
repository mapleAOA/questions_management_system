package com.xyz.question_bank_management_system.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final String roleCode;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String normalized = roleCode == null ? "" : roleCode.trim().toUpperCase();
        if (normalized.isEmpty()) {
            return List.of();
        }
        String authority = normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
        return List.of(new SimpleGrantedAuthority(authority));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
