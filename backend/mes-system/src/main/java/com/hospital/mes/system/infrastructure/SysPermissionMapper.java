package com.hospital.mes.system.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermissionEntity> {
    @Update("""
        UPDATE sys_permission SET permission_type = #{permissionType}, display_name = #{displayName},
          menu_route = #{menuRoute}, parent_permission_id = #{parentPermissionId}, enabled = #{enabled},
          version = version + 1, updated_at = CURRENT_TIMESTAMP(6)
        WHERE id = #{id} AND version = #{version}
        """)
    int updateContract(@Param("id") long id, @Param("version") long version,
                       @Param("permissionType") String permissionType,
                       @Param("displayName") String displayName,
                       @Param("menuRoute") String menuRoute,
                       @Param("parentPermissionId") Long parentPermissionId,
                       @Param("enabled") boolean enabled);
}
