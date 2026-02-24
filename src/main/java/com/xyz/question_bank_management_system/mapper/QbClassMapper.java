package com.xyz.question_bank_management_system.mapper;

import com.xyz.question_bank_management_system.entity.QbClass;
import com.xyz.question_bank_management_system.vo.StudentClassItemVO;
import com.xyz.question_bank_management_system.vo.TeacherClassItemVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QbClassMapper {

    @Insert("INSERT INTO qb_class(class_name, class_code, class_desc, teacher_id, created_at, updated_at, is_deleted) " +
            "VALUES(#{className}, #{classCode}, #{classDesc}, #{teacherId}, NOW(3), NOW(3), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(QbClass qbClass);

    @Select("SELECT * FROM qb_class WHERE class_code=#{classCode} AND is_deleted=0 LIMIT 1")
    QbClass selectByClassCode(@Param("classCode") String classCode);

    @Select("SELECT * FROM qb_class WHERE id=#{classId} AND is_deleted=0")
    QbClass selectById(@Param("classId") Long classId);

    @Select("SELECT c.id, c.class_name, c.class_code, c.class_desc, c.created_at, " +
            "       (SELECT COUNT(1) FROM qb_class_member m WHERE m.class_id=c.id) AS student_count " +
            "FROM qb_class c " +
            "WHERE c.teacher_id=#{teacherId} AND c.is_deleted=0 " +
            "ORDER BY c.created_at DESC, c.id DESC")
    List<TeacherClassItemVO> listByTeacher(@Param("teacherId") Long teacherId);

    @Select("SELECT c.id, c.class_name, c.class_code, c.class_desc, c.teacher_id, u.display_name AS teacher_name, m.joined_at " +
            "FROM qb_class_member m " +
            "JOIN qb_class c ON c.id=m.class_id " +
            "JOIN sys_user u ON u.id=c.teacher_id AND u.is_deleted=0 " +
            "WHERE m.student_id=#{studentId} AND c.is_deleted=0 " +
            "ORDER BY m.joined_at DESC, c.id DESC")
    List<StudentClassItemVO> listByStudent(@Param("studentId") Long studentId);
}
