package com.hospital.mes.system.application;

import com.hospital.mes.common.exception.ResourceConflictException;
import com.hospital.mes.system.infrastructure.SysRoleMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
public class AdminSafetyGuard {
    private final SysRoleMapper roles;

    public AdminSafetyGuard(SysRoleMapper roles) {
        this.roles = roles;
    }

    public void runPreservingEffectiveAdmin(Runnable change) {
        if (roles.lockAdministratorRole() == null) {
            throw new ResourceConflictException("LAST_SYSTEM_ADMIN", "System administrator role is unavailable");
        }
        change.run();
        if (roles.effectiveAdministratorCount() < 1) {
            throw new ResourceConflictException("LAST_SYSTEM_ADMIN", "The last system administrator must remain active");
        }
    }
}
