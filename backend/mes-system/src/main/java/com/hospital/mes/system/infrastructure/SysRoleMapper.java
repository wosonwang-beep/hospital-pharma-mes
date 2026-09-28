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
        WHERE r.role_code = 'SYSTEM_ADMIN' AND ur.revoked_at IS NULL
        """)
    int administratorCount();
}
