package com.hospital.mes.security.identity;

import java.util.Set;
import com.hospital.mes.audit.application.PlatformPrincipal;

public record LoginSnapshot(long userId, long organizationId, String loginName, String displayName,
                            Set<String> roleCodes, Set<String> permissionCodes,
                            boolean mustChangePassword) implements PlatformPrincipal {
    public LoginSnapshot {
        if (organizationId <= 0) throw new IllegalArgumentException("organization context is required");
        roleCodes = Set.copyOf(roleCodes);
        permissionCodes = Set.copyOf(permissionCodes);
    }
}
