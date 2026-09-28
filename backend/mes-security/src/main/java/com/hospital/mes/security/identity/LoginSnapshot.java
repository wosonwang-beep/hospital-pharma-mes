package com.hospital.mes.security.identity;

import java.util.Set;

public record LoginSnapshot(long userId, String loginName, String displayName,
                            Set<String> roleCodes, Set<String> permissionCodes,
                            boolean mustChangePassword) {
    public LoginSnapshot {
        roleCodes = Set.copyOf(roleCodes);
        permissionCodes = Set.copyOf(permissionCodes);
    }
}
