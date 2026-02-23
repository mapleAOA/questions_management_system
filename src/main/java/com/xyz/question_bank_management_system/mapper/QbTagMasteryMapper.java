package com.xyz.question_bank_management_system.mapper;

import com.xyz.question_bank_management_system.entity.QbTagMastery;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QbTagMasteryMapper {

    @Insert("INSERT INTO qb_tag_mastery(user_id, tag_id, mastery_value, correct_count, attempt_count, updated_at) " +
            "VALUES(#{userId}, #{tagId}, #{initMastery}, #{correctInc}, 1, NOW(3)) " +
            "ON DUPLICATE KEY UPDATE correct_count=correct_count+#{correctInc}, attempt_count=attempt_count+1, mastery_value=(correct_count+#{correctInc})/(attempt_count+1), updated_at=NOW(3)")
    int upsertAttempt(@Param("userId") Long userId, @Param("tagId") Long tagId, @Param("correctInc") int correctInc, @Param("initMastery") double initMastery);

    @Select("SELECT * FROM qb_tag_mastery WHERE user_id=#{userId} ORDER BY updated_at DESC")
    List<QbTagMastery> selectByUserId(@Param("userId") Long userId);
}
