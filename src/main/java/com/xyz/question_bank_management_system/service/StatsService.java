package com.xyz.question_bank_management_system.service;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.entity.*;

import java.util.List;

public interface StatsService {

    PageResponse<QbWrongQuestion> wrongQuestions(Long userId, Long tagId, String chapter, Boolean isResolved, long page, long size);

    void resolveWrongQuestion(Long userId, Long questionId);

    PageResponse<QbQuestionUserStat> questionStats(Long userId, Long questionId, long page, long size);

    List<QbTagMastery> mastery(Long userId, Integer tagType);

    QbUserAbility ability(Long userId);
}
