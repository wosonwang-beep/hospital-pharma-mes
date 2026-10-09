package com.hospital.mes.system.infrastructure;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysRoleMenuMapper {
    @Select("""
        SELECT DISTINCT m.menu_code FROM sys_role_menu rm JOIN sys_menu m ON m.id=rm.menu_id
        JOIN sys_user_role ur ON ur.role_id=rm.role_id JOIN sys_role r ON r.id=ur.role_id
        WHERE ur.user_id=#{userId} AND rm.org_id=#{orgId} AND r.enabled=1
        """)
    List<String> effectiveMenuCodes(@Param("userId") long userId, @Param("orgId") long orgId);
    @Select("""
        SELECT m.menu_code FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
        WHERE rm.role_id = #{roleId} ORDER BY m.menu_code
        """)
    List<String> menuCodes(@Param("roleId") long roleId);

    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    int deleteForRole(@Param("roleId") long roleId);

    @Insert("""
        INSERT INTO sys_role_menu (org_id, role_id, menu_id, created_by)
        SELECT #{orgId}, #{roleId}, id, #{actorId} FROM sys_menu
        WHERE org_id = #{orgId} AND menu_code = #{menuCode}
        """)
    int grant(@Param("orgId") long orgId, @Param("roleId") long roleId,
              @Param("menuCode") String menuCode, @Param("actorId") long actorId);
}
