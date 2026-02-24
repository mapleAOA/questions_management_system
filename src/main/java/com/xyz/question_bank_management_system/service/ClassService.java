package com.xyz.question_bank_management_system.service;

import com.xyz.question_bank_management_system.dto.ClassCreateRequest;
import com.xyz.question_bank_management_system.dto.JoinClassRequest;
import com.xyz.question_bank_management_system.vo.ClassStudentItemVO;
import com.xyz.question_bank_management_system.vo.StudentClassItemVO;
import com.xyz.question_bank_management_system.vo.TeacherClassItemVO;

import java.util.List;

public interface ClassService {

    Long create(ClassCreateRequest request, Long teacherId);

    List<TeacherClassItemVO> listMine(Long teacherId);

    List<ClassStudentItemVO> listStudents(Long classId, Long currentUserId, boolean isAdmin);

    void joinByCode(JoinClassRequest request, Long studentId);

    List<StudentClassItemVO> listMyClasses(Long studentId);
}
