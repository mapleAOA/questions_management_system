package com.xyz.question_bank_management_system.mapper;

import com.xyz.question_bank_management_system.entity.QbQuestionUserStat;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface QbQuestionUserStatMapper {

    @Insert("INSERT INTO qb_question_user_stat(user_id, question_id, attempt_count, correct_count, last_attempt_at) " +
            "VALUES(#{userId}, #{questionId}, 1, #{correctInc}, #{at}) " +
            "ON DUPLICATE KEY UPDATE attempt_count=attempt_count+1, correct_count=correct_count+#{correctInc}, last_attempt_at=#{at}")
    int upsert(@Param("userId") Long userId, @Param("questionId") Long questionId, @Param("correctInc") int correctInc, @Param("at") LocalDateTime at);

    @Select("SELECT * FROM qb_question_user_stat WHERE user_id=#{userId} ORDER BY last_attempt_at DESC")
    List<QbQuestionUserStat> selectByUserId(@Param("userId") Long userId);
}
