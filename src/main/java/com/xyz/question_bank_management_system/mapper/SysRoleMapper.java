package com.xyz.question_bank_management_system.mapper;

import com.xyz.question_bank_management_system.entity.SysRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysRoleMapper {

    @Select("SELECT r.role_code FROM sys_role r \n" +
            "JOIN sys_user_role ur ON ur.role_id=r.id \n" +
            "WHERE ur.user_id=#{userId}")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM sys_role ORDER BY id")
    List<SysRole> selectAll();

    @Select("SELECT * FROM sys_role WHERE role_code=#{roleCode} LIMIT 1")
    SysRole selectByCode(@Param("roleCode") String roleCode);

    @Insert("INSERT INTO sys_role(role_code, role_name, created_at, updated_at) VALUES(#{roleCode}, #{roleName}, NOW(3), NOW(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysRole role);
}
