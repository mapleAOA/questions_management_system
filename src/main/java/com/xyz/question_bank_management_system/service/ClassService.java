package com.xyz.question_bank_management_system.service;

import com.xyz.question_bank_management_system.dto.ClassCreateRequest;
import com.xyz.question_bank_management_system.dto.JoinClassRequest;
import com.xyz.question_bank_management_system.vo.ClassStudentItemVO;
import com.xyz.question_bank_management_system.vo.StudentClassItemVO;
import com.xyz.question_bank_management_system.vo.TeacherOptionVO;
import com.xyz.question_bank_management_system.vo.TeacherClassItemVO;

import java.util.List;

public interface ClassService {

    Long create(ClassCreateRequest request, Long currentUserId, boolean isAdmin);

    void update(Long classId, ClassCreateRequest request, Long currentUserId, boolean isAdmin);

    void delete(Long classId, Long currentUserId, boolean isAdmin);

    List<TeacherClassItemVO> listManageable(Long currentUserId, boolean isAdmin);

    List<ClassStudentItemVO> listStudents(Long classId, Long currentUserId, boolean isAdmin);

    void removeStudent(Long classId, Long studentId, Long currentUserId, boolean isAdmin);

    void joinByCode(JoinClassRequest request, Long studentId);

    List<StudentClassItemVO> listMyClasses(Long studentId);

    List<TeacherOptionVO> listTeacherOptions();
}
