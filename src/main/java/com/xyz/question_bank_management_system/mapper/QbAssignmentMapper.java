package com.xyz.question_bank_management_system.mapper;

import com.xyz.question_bank_management_system.entity.QbAssignment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QbAssignmentMapper {

    @Insert("INSERT INTO qb_assignment(paper_id, assignment_title, assignment_desc, start_time, end_time, time_limit_min, max_attempts, shuffle_questions, shuffle_options, publish_status, created_by, created_at, updated_at, is_deleted) " +
            "VALUES(#{paperId}, #{assignmentTitle}, #{assignmentDesc}, #{startTime}, #{endTime}, #{timeLimitMin}, #{maxAttempts}, #{shuffleQuestions}, #{shuffleOptions}, #{publishStatus}, #{createdBy}, NOW(3), NOW(3), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(QbAssignment a);

    @Update("UPDATE qb_assignment SET assignment_title=#{assignmentTitle}, assignment_desc=#{assignmentDesc}, start_time=#{startTime}, end_time=#{endTime}, time_limit_min=#{timeLimitMin}, max_attempts=#{maxAttempts}, shuffle_questions=#{shuffleQuestions}, shuffle_options=#{shuffleOptions}, publish_status=#{publishStatus}, updated_at=NOW(3) WHERE id=#{id} AND is_deleted=0")
    int update(QbAssignment a);

    @Select("SELECT * FROM qb_assignment WHERE id=#{id} AND is_deleted=0")
    QbAssignment selectById(@Param("id") Long id);

    @Update("UPDATE qb_assignment SET is_deleted=1, updated_at=NOW(3) WHERE id=#{id}")
    int softDelete(@Param("id") Long id);

    @Update("UPDATE qb_assignment SET publish_status=#{status}, updated_at=NOW(3) WHERE id=#{id} AND is_deleted=0")
    int updatePublishStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("SELECT * FROM qb_assignment WHERE is_deleted=0 ORDER BY created_at DESC, id DESC LIMIT #{offset}, #{size}")
    List<QbAssignment> pageAll(@Param("offset") long offset, @Param("size") long size);

    @Select("SELECT COUNT(1) FROM qb_assignment WHERE is_deleted=0")
    long countAll();

    @Select("SELECT * FROM qb_assignment WHERE created_by=#{teacherId} AND is_deleted=0 ORDER BY created_at DESC, id DESC LIMIT #{offset}, #{size}")
    List<QbAssignment> pageByTeacher(@Param("teacherId") Long teacherId, @Param("offset") long offset, @Param("size") long size);

    @Select("SELECT COUNT(1) FROM qb_assignment WHERE created_by=#{teacherId} AND is_deleted=0")
    long countByTeacher(@Param("teacherId") Long teacherId);
}
