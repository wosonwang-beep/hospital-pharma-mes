package com.hospital.mes.system.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRoleEntity> {
    @Insert("INSERT INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    int grant(@Param("userId") long userId, @Param("roleId") long roleId);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int revoke(@Param("userId") long userId, @Param("roleId") long roleId);

    @Select("SELECT COUNT(*) FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int pairCount(@Param("userId") long userId, @Param("roleId") long roleId);

    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId} ORDER BY role_id")
    List<Long> activeRoleIds(@Param("userId") long userId);

    @Select("""
        SELECT COUNT(*) FROM sys_user_role ur JOIN sys_role r ON r.id = ur.role_id
        WHERE ur.user_id = #{userId} AND r.role_code = 'SYSTEM_ADMIN'
        """)
    int systemAdminAssignmentCount(@Param("userId") long userId);
}
