package com.xyz.question_bank_management_system.mapper;

import com.xyz.question_bank_management_system.entity.SysUser;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SysUserMapper {

    @Select("SELECT * FROM sys_user WHERE username=#{username} AND is_deleted=0 LIMIT 1")
    SysUser selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM sys_user WHERE id=#{id} AND is_deleted=0")
    SysUser selectById(@Param("id") Long id);

    @Insert("INSERT INTO sys_user(username, password_hash, display_name, email, status, created_at, updated_at, is_deleted) "
            + "VALUES(#{username}, #{passwordHash}, #{displayName}, #{email}, #{status}, NOW(3), NOW(3), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysUser user);

    @Update("UPDATE sys_user SET display_name=#{displayName}, email=#{email}, status=#{status}, updated_at=NOW(3) "
            + "WHERE id=#{id} AND is_deleted=0")
    int update(SysUser user);

    @Update("UPDATE sys_user SET last_login_at=#{time} WHERE id=#{id} AND is_deleted=0")
    int updateLastLoginAt(@Param("id") Long id, @Param("time") LocalDateTime time);

    @Update("UPDATE sys_user SET is_deleted=1, updated_at=NOW(3) WHERE id=#{id}")
    int softDelete(@Param("id") Long id);

    @Select("SELECT * FROM sys_user WHERE is_deleted=0 ORDER BY id DESC LIMIT #{offset}, #{size}")
    List<SysUser> page(@Param("offset") long offset, @Param("size") long size);

    @Select("SELECT COUNT(1) FROM sys_user WHERE is_deleted=0")
    long countAll();
}
