package com.xyz.question_bank_management_system.mapper;

import com.xyz.question_bank_management_system.entity.QbQuestionCase;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QbQuestionCaseMapper {

    @Select("SELECT * FROM qb_question_case WHERE question_id=#{questionId} ORDER BY case_no ASC")
    List<QbQuestionCase> selectByQuestionId(@Param("questionId") Long questionId);

    @Insert("INSERT INTO qb_question_case(question_id, case_no, input_data, expected_output, case_score, is_sample) " +
            "VALUES(#{questionId}, #{caseNo}, #{inputData}, #{expectedOutput}, #{caseScore}, #{isSample})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(QbQuestionCase c);

    @Delete("DELETE FROM qb_question_case WHERE question_id=#{questionId}")
    int deleteByQuestionId(@Param("questionId") Long questionId);
}
