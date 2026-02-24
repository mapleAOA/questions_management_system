package com.xyz.question_bank_management_system.controller;

import com.xyz.question_bank_management_system.common.ApiResponse;
import com.xyz.question_bank_management_system.dto.ClassCreateRequest;
import com.xyz.question_bank_management_system.dto.JoinClassRequest;
import com.xyz.question_bank_management_system.service.ClassService;
import com.xyz.question_bank_management_system.util.SecurityContextUtil;
import com.xyz.question_bank_management_system.vo.ClassStudentItemVO;
import com.xyz.question_bank_management_system.vo.StudentClassItemVO;
import com.xyz.question_bank_management_system.vo.TeacherClassItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
public class ClassController {

    private final ClassService classService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Long> create(@RequestBody @Valid ClassCreateRequest request) {
        Long uid = SecurityContextUtil.getUserId();
        return ApiResponse.ok(classService.create(request, uid));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<List<TeacherClassItemVO>> mine() {
        Long uid = SecurityContextUtil.getUserId();
        return ApiResponse.ok(classService.listMine(uid));
    }

    @GetMapping("/{classId}/students")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<List<ClassStudentItemVO>> students(@PathVariable Long classId) {
        Long uid = SecurityContextUtil.getUserId();
        boolean isAdmin = hasRole("ROLE_ADMIN");
        return ApiResponse.ok(classService.listStudents(classId, uid, isAdmin));
    }

    @PostMapping("/join")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Void> join(@RequestBody @Valid JoinClassRequest request) {
        Long uid = SecurityContextUtil.getUserId();
        classService.joinByCode(request, uid);
        return ApiResponse.ok();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<StudentClassItemVO>> my() {
        Long uid = SecurityContextUtil.getUserId();
        return ApiResponse.ok(classService.listMyClasses(uid));
    }

    private boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(role));
    }
}
