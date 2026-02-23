package com.xyz.question_bank_management_system.service;

import com.xyz.question_bank_management_system.entity.QbLlmCall;

public interface LlmService {

    /**
     * bizType: 1=QUESTION_ANALYSIS,2=SUBJECTIVE_GRADING,3=OTHER
     */
    QbLlmCall chatCompletion(int bizType, long bizId, String prompt);

    /**
     * 从 OpenAI-compatible 响应中抽取文本内容（choices[0].message.content）。
     */
    String extractContent(String responseText);
}
