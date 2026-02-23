package com.xyz.question_bank_management_system.mapper;

import com.xyz.question_bank_management_system.entity.SysLoginLog;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SysLoginLogMapper {

    @Insert("INSERT INTO sys_login_log(user_id, username, success_flag, fail_reason, ip_addr, user_agent, login_at) " +
            "VALUES(#{userId}, #{username}, #{successFlag}, #{failReason}, #{ipAddr}, #{userAgent}, NOW(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysLoginLog log);
}
