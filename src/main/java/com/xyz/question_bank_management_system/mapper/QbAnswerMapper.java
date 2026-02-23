package com.xyz.question_bank_management_system.mapper;

import com.xyz.question_bank_management_system.entity.QbAnswer;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface QbAnswerMapper {

    @Insert("INSERT INTO qb_answer(attempt_id, attempt_question_id, question_id, user_id, answer_content, answer_format, answer_status, auto_score, final_score, is_correct) " +
            "VALUES(#{attemptId}, #{attemptQuestionId}, #{questionId}, #{userId}, #{answerContent}, #{answerFormat}, #{answerStatus}, #{autoScore}, #{finalScore}, #{isCorrect})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(QbAnswer answer);

    @Select("SELECT * FROM qb_answer WHERE id=#{id}")
    QbAnswer selectById(@Param("id") Long id);

    @Select("SELECT * FROM qb_answer WHERE attempt_id=#{attemptId} ORDER BY attempt_question_id ASC")
    List<QbAnswer> selectByAttemptId(@Param("attemptId") Long attemptId);

    @Update("UPDATE qb_answer SET answer_content=#{answerContent}, answer_status=1 WHERE id=#{id}")
    int updateDraft(@Param("id") Long id, @Param("answerContent") String answerContent);

    @Update("UPDATE qb_answer SET answer_status=2, answered_at=#{answeredAt} WHERE attempt_id=#{attemptId}")
    int submitAllByAttempt(@Param("attemptId") Long attemptId, @Param("answeredAt") LocalDateTime answeredAt);

    @Update("UPDATE qb_answer SET auto_score=#{autoScore}, final_score=#{finalScore}, is_correct=#{isCorrect}, graded_at=#{gradedAt} WHERE id=#{id}")
    int updateScoring(@Param("id") Long id,
                      @Param("autoScore") Integer autoScore,
                      @Param("finalScore") Integer finalScore,
                      @Param("isCorrect") Integer isCorrect,
                      @Param("gradedAt") LocalDateTime gradedAt);
}
