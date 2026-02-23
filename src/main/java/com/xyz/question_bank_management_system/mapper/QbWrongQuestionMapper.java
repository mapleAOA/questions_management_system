package com.xyz.question_bank_management_system.mapper;

import com.xyz.question_bank_management_system.entity.QbWrongQuestion;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface QbWrongQuestionMapper {

    @Insert("INSERT INTO qb_wrong_question(user_id, question_id, wrong_count, first_wrong_at, last_wrong_at, is_resolved) " +
            "VALUES(#{userId}, #{questionId}, 1, #{at}, #{at}, 0) " +
            "ON DUPLICATE KEY UPDATE wrong_count=wrong_count+1, last_wrong_at=#{at}, is_resolved=0")
    int upsertWrong(@Param("userId") Long userId, @Param("questionId") Long questionId, @Param("at") LocalDateTime at);

    @Update("UPDATE qb_wrong_question SET is_resolved=1, resolved_at=#{at} WHERE user_id=#{userId} AND question_id=#{questionId}")
    int resolve(@Param("userId") Long userId, @Param("questionId") Long questionId, @Param("at") LocalDateTime at);

    @Select("SELECT * FROM qb_wrong_question WHERE user_id=#{userId} ORDER BY last_wrong_at DESC")
    List<QbWrongQuestion> selectByUserId(@Param("userId") Long userId);
}
