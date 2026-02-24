package com.xyz.question_bank_management_system.mapper;

import com.xyz.question_bank_management_system.entity.QbAttempt;
import com.xyz.question_bank_management_system.vo.TeacherAssignmentScoreItemVO;
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

    @Update("UPDATE qb_attempt SET total_score = total_score + #{deltaTotal}, objective_score = objective_score + #{deltaObjective}, " +
            "subjective_score = subjective_score + #{deltaSubjective}, updated_at=NOW(3) WHERE id=#{id}")
    int updateScoreDelta(@Param("id") Long id,
                         @Param("deltaTotal") Integer deltaTotal,
                         @Param("deltaObjective") Integer deltaObjective,
                         @Param("deltaSubjective") Integer deltaSubjective);

    @Select({
            "<script>",
            "SELECT *",
            "FROM qb_attempt",
            "WHERE user_id = #{userId}",
            "<if test='attemptType != null'>",
            "  AND attempt_type = #{attemptType}",
            "</if>",
            "ORDER BY created_at DESC, id DESC",
            "LIMIT #{offset}, #{size}",
            "</script>"
    })
    List<QbAttempt> pageByUser(@Param("userId") Long userId,
                               @Param("attemptType") Integer attemptType,
                               @Param("offset") long offset,
                               @Param("size") long size);

    @Select({
            "<script>",
            "SELECT COUNT(1)",
            "FROM qb_attempt",
            "WHERE user_id = #{userId}",
            "<if test='attemptType != null'>",
            "  AND attempt_type = #{attemptType}",
            "</if>",
            "</script>"
    })
    long countByUser(@Param("userId") Long userId, @Param("attemptType") Integer attemptType);

    @Select("SELECT COUNT(1) FROM qb_attempt WHERE assignment_id=#{assignmentId} AND status IN (2,3,4)")
    long countByAssignmentForTeacher(@Param("assignmentId") Long assignmentId);

    @Select("SELECT a.user_id AS student_id, u.display_name, a.id AS attempt_id, a.total_score, a.needs_review, a.submitted_at " +
            "FROM qb_attempt a LEFT JOIN sys_user u ON u.id = a.user_id " +
            "WHERE a.assignment_id=#{assignmentId} AND a.status IN (2,3,4) " +
            "ORDER BY a.submitted_at DESC, a.id DESC LIMIT #{offset}, #{size}")
    List<TeacherAssignmentScoreItemVO> pageByAssignmentForTeacher(@Param("assignmentId") Long assignmentId,
                                                                  @Param("offset") long offset,
                                                                  @Param("size") long size);
}
