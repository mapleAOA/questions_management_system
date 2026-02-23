package com.xyz.question_bank_management_system.service.impl;

import com.xyz.question_bank_management_system.entity.*;
import com.xyz.question_bank_management_system.mapper.*;
import com.xyz.question_bank_management_system.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final QbWrongQuestionMapper wrongQuestionMapper;
    private final QbQuestionUserStatMapper questionUserStatMapper;
    private final QbTagMasteryMapper tagMasteryMapper;
    private final QbUserAbilityMapper userAbilityMapper;

    @Override
    public List<QbWrongQuestion> wrongQuestions(Long userId) {
        //LC待写
        //return wrongQuestionMapper.listByUser(userId);
        return null;
    }

    @Override
    public void resolveWrongQuestion(Long userId, Long questionId) {
        //LC待写
        //wrongQuestionMapper.resolve(userId, questionId);
    }

    @Override
    public List<QbQuestionUserStat> questionStats(Long userId) {
        //LC待写
        return null;
        //return questionUserStatMapper.listByUser(userId);
    }

    @Override
    public List<QbTagMastery> mastery(Long userId) {
        //LC待写
        return null;
        //return tagMasteryMapper.listByUser(userId);
    }

    @Override
    public QbUserAbility ability(Long userId) {
        return userAbilityMapper.selectByUserId(userId);
    }
}
