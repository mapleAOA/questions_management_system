package com.xyz.question_bank_management_system.service;

import com.xyz.question_bank_management_system.common.PageResponse;
import com.xyz.question_bank_management_system.dto.AppealCreateRequest;
import com.xyz.question_bank_management_system.dto.AppealHandleRequest;
import com.xyz.question_bank_management_system.vo.AppealMyItemVO;
import com.xyz.question_bank_management_system.vo.TeacherAppealItemVO;

public interface AppealService {

    Long submitAppeal(AppealCreateRequest request, Long userId);

    PageResponse<AppealMyItemVO> pageMyAppeals(Long userId, Integer status, long page, long size);

    PageResponse<TeacherAppealItemVO> pageTeacherAppeals(Integer status, long page, long size);

    void handleAppeal(Long appealId, AppealHandleRequest request, Long handlerId);
}
