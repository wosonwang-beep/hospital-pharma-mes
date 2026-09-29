package com.hospital.mes.system.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRoleEntity> {
    @Update("""
        UPDATE sys_role SET display_name = #{displayName}, version = version + 1,
          updated_at = CURRENT_TIMESTAMP(6), updated_by = #{actorId}
        WHERE id = #{id} AND version = #{version}
        """)
    int updateDisplayName(@Param("id") long id, @Param("version") long version,
                          @Param("displayName") String displayName, @Param("actorId") long actorId);

    @Update("""
        UPDATE sys_role SET enabled = #{enabled}, version = version + 1,
          updated_at = CURRENT_TIMESTAMP(6), updated_by = #{actorId}
        WHERE id = #{id} AND version = #{version}
        """)
    int updateEnabled(@Param("id") long id, @Param("version") long version,
                      @Param("enabled") boolean enabled, @Param("actorId") long actorId);

    @Update("""
        UPDATE sys_role SET display_name = #{displayName}, enabled = #{enabled},
          version = version + 1, updated_at = CURRENT_TIMESTAMP(6), updated_by = #{actorId}
        WHERE id = #{id} AND version = #{version}
        """)
    int updateContract(@Param("id") long id, @Param("version") long version,
                       @Param("displayName") String displayName, @Param("enabled") boolean enabled,
                       @Param("actorId") long actorId);

    @Update("""
        UPDATE sys_role SET version = version + 1, updated_at = CURRENT_TIMESTAMP(6), updated_by = #{actorId}
        WHERE id = #{id} AND version = #{version}
        """)
    int touchVersion(@Param("id") long id, @Param("version") long version, @Param("actorId") long actorId);

    @Select("SELECT id FROM sys_role WHERE role_code = 'SYSTEM_ADMIN' FOR UPDATE")
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
