package com.xyz.question_bank_management_system.service.impl;

import com.xyz.question_bank_management_system.dto.PracticeStartRequest;
import com.xyz.question_bank_management_system.exception.BizException;
import com.xyz.question_bank_management_system.exception.ErrorCode;
import com.xyz.question_bank_management_system.mapper.*;
import com.xyz.question_bank_management_system.service.LlmService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AttemptServiceImplValidationTest {

    @Mock
    private QbAssignmentMapper assignmentMapper;
    @Mock
    private QbAssignmentTargetMapper targetMapper;
    @Mock
    private QbAttemptMapper attemptMapper;
    @Mock
    private QbAttemptQuestionMapper attemptQuestionMapper;
    @Mock
    private QbAnswerMapper answerMapper;
    @Mock
    private QbGradingRecordMapper gradingRecordMapper;
    @Mock
    private QbPaperQuestionMapper paperQuestionMapper;
    @Mock
    private QbQuestionMapper questionMapper;
    @Mock
    private QbQuestionOptionMapper optionMapper;
    @Mock
    private QbQuestionCaseMapper caseMapper;
    @Mock
    private QbQuestionTagMapper questionTagMapper;
    @Mock
    private QbQuestionUserStatMapper questionUserStatMapper;
    @Mock
    private QbWrongQuestionMapper wrongQuestionMapper;
    @Mock
    private QbTagMasteryMapper tagMasteryMapper;
    @Mock
    private QbUserAbilityMapper userAbilityMapper;
    @Mock
    private LlmService llmService;

    @InjectMocks
    private AttemptServiceImpl attemptService;

    @Test
    void myAttempts_shouldRejectUnsupportedAttemptType() {
        BizException ex = assertThrows(BizException.class, () -> attemptService.myAttempts(9, 1, 10, 1001L));
        assertEquals(ErrorCode.PARAM_ERROR, ex.getCode());
    }

    @Test
    void startPracticeAttempt_shouldRejectInvalidMode() {
        PracticeStartRequest request = new PracticeStartRequest();
        request.setMode("unknown");
        request.setTotalScore(100);

        BizException ex = assertThrows(BizException.class, () -> attemptService.startPracticeAttempt(request, 1001L));
        assertEquals(ErrorCode.PARAM_ERROR, ex.getCode());
    }
}
