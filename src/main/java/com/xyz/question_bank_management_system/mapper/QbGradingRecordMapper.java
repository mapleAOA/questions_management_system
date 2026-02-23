package com.xyz.question_bank_management_system.mapper;

import com.xyz.question_bank_management_system.entity.QbGradingRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QbGradingRecordMapper {

    @Insert("INSERT INTO qb_grading_record(answer_id, grading_mode, score, detail_json, llm_call_id, confidence, needs_review, reviewer_id, review_comment, is_final, created_at) " +
            "VALUES(#{answerId}, #{gradingMode}, #{score}, #{detailJson}, #{llmCallId}, #{confidence}, #{needsReview}, #{reviewerId}, #{reviewComment}, #{isFinal}, NOW(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(QbGradingRecord record);

    @Select("SELECT * FROM qb_grading_record WHERE answer_id=#{answerId} ORDER BY created_at ASC")
    List<QbGradingRecord> selectByAnswerId(@Param("answerId") Long answerId);
}
