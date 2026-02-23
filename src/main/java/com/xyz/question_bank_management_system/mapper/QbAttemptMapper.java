package com.xyz.question_bank_management_system.mapper;

import com.xyz.question_bank_management_system.entity.QbAttempt;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QbAttemptMapper {

    @Insert("INSERT INTO qb_attempt(assignment_id, paper_id, user_id, attempt_type, attempt_no, status, started_at, total_score, objective_score, subjective_score, needs_review, created_at, updated_at) " +
            "VALUES(#{assignmentId}, #{paperId}, #{userId}, #{attemptType}, #{attemptNo}, #{status}, NOW(3), 0, 0, 0, 0, NOW(3), NOW(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(QbAttempt attempt);

    @Select("SELECT * FROM qb_attempt WHERE id=#{id}")
    QbAttempt selectById(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM qb_attempt WHERE assignment_id=#{assignmentId} AND user_id=#{userId}")
    long countByAssignmentAndUser(@Param("assignmentId") Long assignmentId, @Param("userId") Long userId);

    @Update("UPDATE qb_attempt SET status=#{status}, submitted_at=#{submittedAt}, duration_sec=#{durationSec}, total_score=#{totalScore}, objective_score=#{objectiveScore}, subjective_score=#{subjectiveScore}, needs_review=#{needsReview}, updated_at=NOW(3) WHERE id=#{id}")
    int updateAfterSubmit(QbAttempt attempt);

    @Update("UPDATE qb_attempt SET status=#{status}, updated_at=NOW(3) WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("SELECT * FROM qb_attempt WHERE user_id=#{userId} ORDER BY created_at DESC, id DESC LIMIT #{offset}, #{size}")
    List<QbAttempt> pageByUser(@Param("userId") Long userId, @Param("offset") long offset, @Param("size") long size);

    @Select("SELECT COUNT(1) FROM qb_attempt WHERE user_id=#{userId}")
    long countByUser(@Param("userId") Long userId);
}
