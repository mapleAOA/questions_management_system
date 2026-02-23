package com.xyz.question_bank_management_system.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QbQuestionTagMapper {

    @Select("SELECT tag_id FROM qb_question_tag WHERE question_id=#{questionId}")
    List<Long> selectTagIdsByQuestionId(@Param("questionId") Long questionId);

    @Delete("DELETE FROM qb_question_tag WHERE question_id=#{questionId}")
    int deleteByQuestionId(@Param("questionId") Long questionId);

    int batchInsert(@Param("questionId") Long questionId, @Param("tagIds") List<Long> tagIds);
}
