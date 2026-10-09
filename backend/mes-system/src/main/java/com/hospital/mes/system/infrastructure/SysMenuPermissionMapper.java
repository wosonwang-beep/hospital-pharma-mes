package com.hospital.mes.system.infrastructure;
import java.util.List;
import org.apache.ibatis.annotations.*;
@Mapper
public interface SysMenuPermissionMapper {
    @Select("SELECT p.permission_code FROM sys_menu_permission mp JOIN sys_permission p ON p.id=mp.permission_id WHERE mp.menu_id=#{menuId} ORDER BY p.id")
    List<String> codes(@Param("menuId") long menuId);
    @Delete("DELETE FROM sys_menu_permission WHERE menu_id=#{menuId}")
    int clear(@Param("menuId") long menuId);
    @Insert("INSERT INTO sys_menu_permission(menu_id,permission_id,created_by) VALUES(#{menuId},#{permissionId},#{actorId})")
    int bind(@Param("menuId") long menuId,@Param("permissionId") long permissionId,@Param("actorId") long actorId);
}
