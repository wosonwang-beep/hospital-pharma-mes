package com.hospital.mes.system.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenuEntity> {
    @Update("""
        UPDATE sys_menu SET parent_id = #{parentId}, menu_name = #{menuName}, route_path = #{routePath},
          sort_no = #{sortNo}, status = #{status}, updated_by = #{actorId},
          updated_at = CURRENT_TIMESTAMP(3), version_no = version_no + 1
        WHERE id = #{id} AND org_id = #{orgId} AND version_no = #{version}
        """)
    int updateContract(@Param("id") long id, @Param("orgId") long orgId,
                       @Param("version") long version, @Param("parentId") Long parentId,
                       @Param("menuName") String menuName, @Param("routePath") String routePath,
                       @Param("sortNo") int sortNo, @Param("status") String status,
                       @Param("actorId") long actorId);
}
