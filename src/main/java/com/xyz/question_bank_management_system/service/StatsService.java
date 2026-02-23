package com.xyz.question_bank_management_system.service;

import com.xyz.question_bank_management_system.entity.*;

import java.util.List;

public interface StatsService {

    List<QbWrongQuestion> wrongQuestions(Long userId);

    void resolveWrongQuestion(Long userId, Long questionId);

    List<QbQuestionUserStat> questionStats(Long userId);

    List<QbTagMastery> mastery(Long userId);

    QbUserAbility ability(Long userId);
}
