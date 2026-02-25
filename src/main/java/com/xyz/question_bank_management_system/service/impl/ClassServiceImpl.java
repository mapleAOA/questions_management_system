package com.xyz.question_bank_management_system.service.impl;

import com.xyz.question_bank_management_system.dto.ClassCreateRequest;
import com.xyz.question_bank_management_system.dto.JoinClassRequest;
import com.xyz.question_bank_management_system.entity.QbClass;
import com.xyz.question_bank_management_system.exception.BizException;
import com.xyz.question_bank_management_system.exception.ErrorCode;
import com.xyz.question_bank_management_system.mapper.QbClassMapper;
import com.xyz.question_bank_management_system.mapper.QbClassMemberMapper;
import com.xyz.question_bank_management_system.service.ClassService;
import com.xyz.question_bank_management_system.vo.ClassStudentItemVO;
import com.xyz.question_bank_management_system.vo.StudentClassItemVO;
import com.xyz.question_bank_management_system.vo.TeacherClassItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;

    private final QbClassMapper classMapper;
    private final QbClassMemberMapper classMemberMapper;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public Long create(ClassCreateRequest request, Long teacherId) {
        String className = request.getClassName() == null ? null : request.getClassName().trim();
        if (className == null || className.isBlank()) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "className cannot be empty");
        }

        QbClass qbClass = new QbClass();
        qbClass.setClassName(className);
        qbClass.setClassDesc(trimToNull(request.getClassDesc()));
        qbClass.setTeacherId(teacherId);
        qbClass.setClassCode(generateUniqueClassCode());

        classMapper.insert(qbClass);
        return qbClass.getId();
    }

    @Override
    public List<TeacherClassItemVO> listMine(Long teacherId) {
        return classMapper.listByTeacher(teacherId);
    }

    @Override
    public List<ClassStudentItemVO> listStudents(Long classId, Long currentUserId, boolean isAdmin) {
        loadClassForManage(classId, currentUserId, isAdmin);
        return classMemberMapper.listStudentsByClassId(classId);
    }

    @Override
    @Transactional
    public void removeStudent(Long classId, Long studentId, Long currentUserId, boolean isAdmin) {
        if (studentId == null) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "studentId cannot be null");
        }
        loadClassForManage(classId, currentUserId, isAdmin);
        classMemberMapper.removeByClassAndStudent(classId, studentId);
    }

    @Override
    @Transactional
    public void joinByCode(JoinClassRequest request, Long studentId) {
        String classCode = request.getClassCode() == null ? null : request.getClassCode().trim().toUpperCase(Locale.ROOT);
        if (classCode == null || classCode.isBlank()) {
            throw BizException.of(ErrorCode.PARAM_ERROR, "classCode cannot be empty");
        }

        QbClass qbClass = classMapper.selectByClassCode(classCode);
        if (qbClass == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "class not found");
        }

        long joined = classMemberMapper.countByClassAndStudent(qbClass.getId(), studentId);
        if (joined > 0) {
            return;
        }
        classMemberMapper.insert(qbClass.getId(), studentId);
    }

    @Override
    public List<StudentClassItemVO> listMyClasses(Long studentId) {
        return classMapper.listByStudent(studentId);
    }

    private String generateUniqueClassCode() {
        for (int i = 0; i < 10; i++) {
            String classCode = randomClassCode();
            if (classMapper.selectByClassCode(classCode) == null) {
                return classCode;
            }
        }
        throw BizException.of(ErrorCode.CONFLICT, "failed to generate unique class code");
    }

    private String randomClassCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int idx = secureRandom.nextInt(CODE_CHARS.length());
            sb.append(CODE_CHARS.charAt(idx));
        }
        return sb.toString();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private QbClass loadClassForManage(Long classId, Long currentUserId, boolean isAdmin) {
        QbClass qbClass = classMapper.selectById(classId);
        if (qbClass == null) {
            throw BizException.of(ErrorCode.NOT_FOUND, "class not found");
        }
        if (!isAdmin && !Objects.equals(qbClass.getTeacherId(), currentUserId)) {
            throw BizException.of(ErrorCode.FORBIDDEN, "forbidden");
        }
        return qbClass;
    }
}
