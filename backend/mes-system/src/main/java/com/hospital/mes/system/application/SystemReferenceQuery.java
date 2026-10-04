package com.hospital.mes.system.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospital.mes.system.infrastructure.SysRoleEntity;
import com.hospital.mes.system.infrastructure.SysRoleMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** Read-only reference validation; IAM roles retain their existing global scope. */
@Service
@ConditionalOnProperty(prefix="spring.datasource",name="url")
public class SystemReferenceQuery {
    private final SysRoleMapper roles;
    public SystemReferenceQuery(SysRoleMapper roles) { this.roles=roles; }
    public void requireRole(String code) {
        if (code==null || code.isBlank() || roles.selectCount(new QueryWrapper<SysRoleEntity>()
                .eq("role_code",code).eq("enabled",true))==0) {
            throw new java.util.NoSuchElementException("Enabled role not found");
        }
    }
}
