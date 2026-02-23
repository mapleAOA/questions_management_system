package com.xyz.question_bank_management_system.config;

import com.xyz.question_bank_management_system.entity.SysRole;
import com.xyz.question_bank_management_system.entity.SysUser;
import com.xyz.question_bank_management_system.mapper.SysRoleMapper;
import com.xyz.question_bank_management_system.mapper.SysUserMapper;
import com.xyz.question_bank_management_system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitDataRunner implements CommandLineRunner {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.enabled:true}")
    private boolean enabled;

    @Value("${app.init.admin-username:admin}")
    private String adminUsername;

    @Value("${app.init.admin-password:admin123}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        // 1) 确保基础角色存在
        ensureRole("STUDENT", "学生");
        ensureRole("TEACHER", "教师");
        ensureRole("ADMIN", "管理员");

        // 2) 确保有一个管理员账号可登录（开发环境）
        SysUser existing = sysUserMapper.selectByUsername(adminUsername);
        if (existing == null) {
            SysUser u = new SysUser();
            u.setUsername(adminUsername);
            u.setPasswordHash(passwordEncoder.encode(adminPassword));
            u.setDisplayName("系统管理员");
            u.setEmail("admin@example.com");
            u.setStatus(1);
            u.setIsDeleted(0);
            sysUserMapper.insert(u);

            SysRole adminRole = sysRoleMapper.selectByCode("ADMIN");
            if (adminRole != null) {
                sysUserRoleMapper.insert(u.getId(), adminRole.getId());
            }
            log.warn("Initialized default admin user: {} / {} (please change in production)", adminUsername, adminPassword);
        }
    }

    private void ensureRole(String roleCode, String roleName) {
        SysRole role = sysRoleMapper.selectByCode(roleCode);
        if (role == null) {
            SysRole r = new SysRole();
            r.setRoleCode(roleCode);
            r.setRoleName(roleName);
            sysRoleMapper.insert(r);
        }
    }
}
