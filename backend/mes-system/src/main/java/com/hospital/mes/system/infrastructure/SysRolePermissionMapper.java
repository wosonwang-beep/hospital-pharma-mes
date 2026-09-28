package com.hospital.mes.system.infrastructure;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysRolePermissionMapper {
    @Insert("INSERT INTO sys_role_permission (role_id, permission_id) VALUES (#{roleId}, #{permissionId})")
    int grant(@Param("roleId") long roleId, @Param("permissionId") long permissionId);

    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    int revoke(@Param("roleId") long roleId, @Param("permissionId") long permissionId);

    @Select("SELECT COUNT(*) FROM sys_role_permission WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    int pairCount(@Param("roleId") long roleId, @Param("permissionId") long permissionId);

    @Select("""
        SELECT p.permission_code FROM sys_role_permission rp
        JOIN sys_permission p ON p.id = rp.permission_id
        WHERE rp.role_id = #{roleId} ORDER BY p.permission_code
        """)
    List<String> permissionCodes(@Param("roleId") long roleId);

    @Select("""
        SELECT DISTINCT r.role_code FROM sys_role r
        JOIN sys_user_role ur ON ur.role_id = r.id
        WHERE ur.user_id = #{userId} AND r.enabled = TRUE
        """)
    List<String> activeRoleCodes(@Param("userId") long userId);

    @Select("""
        SELECT DISTINCT p.permission_code FROM sys_permission p
        JOIN sys_role_permission rp ON rp.permission_id = p.id
        JOIN sys_role r ON r.id = rp.role_id AND r.enabled = TRUE
        JOIN sys_user_role ur ON ur.role_id = r.id
        WHERE ur.user_id = #{userId} AND p.enabled = TRUE
        """)
    List<String> activePermissionCodes(@Param("userId") long userId);
}
