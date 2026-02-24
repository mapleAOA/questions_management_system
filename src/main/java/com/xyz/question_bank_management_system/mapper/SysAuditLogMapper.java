package com.xyz.question_bank_management_system.mapper;

import com.xyz.question_bank_management_system.entity.SysAuditLog;
import com.xyz.question_bank_management_system.vo.AdminAuditLogItemVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SysAuditLogMapper {

    @Insert("INSERT INTO sys_audit_log(user_id, action, entity_type, entity_id, before_json, after_json, ip_addr, created_at) " +
            "VALUES(#{userId}, #{action}, #{entityType}, #{entityId}, #{beforeJson}, #{afterJson}, #{ipAddr}, NOW(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysAuditLog log);

    @Select({
            "<script>",
            "SELECT COUNT(1)",
            "FROM sys_audit_log",
            "WHERE 1=1",
            "<if test='userId != null'>",
            "  AND user_id = #{userId}",
            "</if>",
            "<if test='action != null and action != \"\"'>",
            "  AND action = #{action}",
            "</if>",
            "<if test='entityType != null and entityType != \"\"'>",
            "  AND entity_type = #{entityType}",
            "</if>",
            "<if test='entityId != null'>",
            "  AND entity_id = #{entityId}",
            "</if>",
            "<if test='startTime != null'>",
            "  AND created_at &gt;= #{startTime}",
            "</if>",
            "<if test='endTime != null'>",
            "  AND created_at &lt;= #{endTime}",
            "</if>",
            "</script>"
    })
    long countByFilter(@Param("userId") Long userId,
                       @Param("action") String action,
                       @Param("entityType") String entityType,
                       @Param("entityId") Long entityId,
                       @Param("startTime") LocalDateTime startTime,
                       @Param("endTime") LocalDateTime endTime);

    @Select({
            "<script>",
            "SELECT id AS log_id, user_id, action, entity_type, entity_id, created_at",
            "FROM sys_audit_log",
            "WHERE 1=1",
            "<if test='userId != null'>",
            "  AND user_id = #{userId}",
            "</if>",
            "<if test='action != null and action != \"\"'>",
            "  AND action = #{action}",
            "</if>",
            "<if test='entityType != null and entityType != \"\"'>",
            "  AND entity_type = #{entityType}",
            "</if>",
            "<if test='entityId != null'>",
            "  AND entity_id = #{entityId}",
            "</if>",
            "<if test='startTime != null'>",
            "  AND created_at &gt;= #{startTime}",
            "</if>",
            "<if test='endTime != null'>",
            "  AND created_at &lt;= #{endTime}",
            "</if>",
            "ORDER BY created_at DESC, id DESC",
            "LIMIT #{offset}, #{size}",
            "</script>"
    })
    List<AdminAuditLogItemVO> pageByFilter(@Param("userId") Long userId,
                                           @Param("action") String action,
                                           @Param("entityType") String entityType,
                                           @Param("entityId") Long entityId,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime,
                                           @Param("offset") long offset,
                                           @Param("size") long size);
}
