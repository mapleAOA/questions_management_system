package com.xyz.question_bank_management_system.mapper;

import com.xyz.question_bank_management_system.entity.QbQuestionCase;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QbQuestionCaseMapper {

    @Select("SELECT * FROM qb_question_case WHERE id=#{id}")
    QbQuestionCase selectById(@Param("id") Long id);

    @Select("SELECT * FROM qb_question_case WHERE question_id=#{questionId} ORDER BY case_no ASC")
    List<QbQuestionCase> selectByQuestionId(@Param("questionId") Long questionId);

    @Select("SELECT * FROM qb_question_case WHERE question_id=#{questionId} AND case_no=#{caseNo} LIMIT 1")
    QbQuestionCase selectByQuestionAndCaseNo(@Param("questionId") Long questionId, @Param("caseNo") Integer caseNo);

    @Insert("INSERT INTO qb_question_case(question_id, case_no, input_data, expected_output, case_score, is_sample) " +
            "VALUES(#{questionId}, #{caseNo}, #{inputData}, #{expectedOutput}, #{caseScore}, #{isSample})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(QbQuestionCase c);

    @Update("UPDATE qb_question_case SET case_no=#{caseNo}, input_data=#{inputData}, expected_output=#{expectedOutput}, case_score=#{caseScore}, is_sample=#{isSample} WHERE id=#{id}")
    int update(QbQuestionCase c);

    @Delete("DELETE FROM qb_question_case WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    @Delete("DELETE FROM qb_question_case WHERE question_id=#{questionId}")
    int deleteByQuestionId(@Param("questionId") Long questionId);
}
