package com.hospital.mes.system.infrastructure;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysRolePermissionMapper {
    @Select("""
        SELECT DISTINCT r.role_code FROM sys_role r
        JOIN sys_user_role ur ON ur.role_id = r.id
        WHERE ur.user_id = #{userId} AND ur.revoked_at IS NULL AND r.enabled = TRUE
        """)
    List<String> activeRoleCodes(@Param("userId") long userId);

    @Select("""
        SELECT DISTINCT p.permission_code FROM sys_permission p
        JOIN sys_role_permission rp ON rp.permission_id = p.id AND rp.revoked_at IS NULL
        JOIN sys_role r ON r.id = rp.role_id AND r.enabled = TRUE
        JOIN sys_user_role ur ON ur.role_id = r.id AND ur.revoked_at IS NULL
        WHERE ur.user_id = #{userId} AND p.enabled = TRUE
        """)
    List<String> activePermissionCodes(@Param("userId") long userId);
}
