package com.xyz.question_bank_management_system.mapper;

import com.xyz.question_bank_management_system.entity.QbLlmCall;
import com.xyz.question_bank_management_system.vo.LlmCallListItemVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QbLlmCallMapper {

    @Insert("INSERT INTO qb_llm_call(biz_type, biz_id, model_name, prompt_text, call_status, created_at) " +
            "VALUES(#{bizType}, #{bizId}, #{modelName}, #{promptText}, #{callStatus}, NOW(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(QbLlmCall call);

    @Update("UPDATE qb_llm_call SET response_text=#{responseText}, response_json=#{responseJson}, call_status=#{callStatus}, latency_ms=#{latencyMs}, tokens_prompt=#{tokensPrompt}, tokens_completion=#{tokensCompletion}, cost_amount=#{costAmount} WHERE id=#{id}")
    int updateResponse(QbLlmCall call);

    @Select("SELECT * FROM qb_llm_call WHERE id=#{id}")
    QbLlmCall selectById(@Param("id") Long id);

    @Select("SELECT * FROM qb_llm_call WHERE biz_type=#{bizType} AND biz_id=#{bizId} ORDER BY created_at DESC")
    List<QbLlmCall> selectByBiz(@Param("bizType") Integer bizType, @Param("bizId") Long bizId);

    @Select({
            "<script>",
            "SELECT COUNT(1)",
            "FROM qb_llm_call",
            "WHERE 1=1",
            "<if test='bizType != null'>",
            "  AND biz_type = #{bizType}",
            "</if>",
            "<if test='bizId != null'>",
            "  AND biz_id = #{bizId}",
            "</if>",
            "</script>"
    })
    long countByFilter(@Param("bizType") Integer bizType, @Param("bizId") Long bizId);

    @Select({
            "<script>",
            "SELECT id AS llm_call_id, biz_type, biz_id, model_name, call_status, latency_ms, created_at",
            "FROM qb_llm_call",
            "WHERE 1=1",
            "<if test='bizType != null'>",
            "  AND biz_type = #{bizType}",
            "</if>",
            "<if test='bizId != null'>",
            "  AND biz_id = #{bizId}",
            "</if>",
            "ORDER BY created_at DESC, id DESC",
            "LIMIT #{offset}, #{size}",
            "</script>"
    })
    List<LlmCallListItemVO> pageByFilter(@Param("bizType") Integer bizType,
                                         @Param("bizId") Long bizId,
                                         @Param("offset") long offset,
                                         @Param("size") long size);
}
