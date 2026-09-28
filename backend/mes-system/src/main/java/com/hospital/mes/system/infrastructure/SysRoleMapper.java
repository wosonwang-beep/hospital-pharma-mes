package com.hospital.mes.system.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRoleEntity> {
    @Select("SELECT id FROM sys_role WHERE role_code = 'SYSTEM_ADMIN' AND enabled = TRUE FOR UPDATE")
    Long lockAdministratorRole();

    @Select("""
        SELECT COUNT(*) FROM sys_user_role ur
        JOIN sys_role r ON r.id = ur.role_id
        JOIN sys_user u ON u.id = ur.user_id
        WHERE r.role_code = 'SYSTEM_ADMIN' AND r.enabled = TRUE AND u.enabled = TRUE
        """)
    int administratorCount();

    @Select("""
        SELECT COUNT(DISTINCT u.id) FROM sys_user u
        JOIN sys_user_role admin_ur ON admin_ur.user_id = u.id
        JOIN sys_role admin_r ON admin_r.id = admin_ur.role_id
        WHERE u.enabled = TRUE AND admin_r.enabled = TRUE AND admin_r.role_code = 'SYSTEM_ADMIN'
        AND (SELECT COUNT(DISTINCT p.permission_code) FROM sys_user_role ur
             JOIN sys_role r ON r.id = ur.role_id AND r.enabled = TRUE
             JOIN sys_role_permission rp ON rp.role_id = r.id
             JOIN sys_permission p ON p.id = rp.permission_id AND p.enabled = TRUE
             WHERE ur.user_id = u.id AND p.permission_code IN
               ('menu:iam:users', 'action:iam:user.manage', 'menu:iam:roles', 'action:iam:role.manage')) = 4
        """)
    int effectiveAdministratorCount();
}
