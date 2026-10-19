package com.hospital.mes.security.identity;

import java.util.Set;
import com.hospital.mes.audit.application.PlatformPrincipal;

public record LoginSnapshot(long userId, long organizationId, String loginName, String displayName,
                            Set<String> roleCodes, Set<String> permissionCodes,
                            boolean mustChangePassword, Set<String> roleIds, int identityFormatVersion) implements PlatformPrincipal {
    public LoginSnapshot(long userId,long organizationId,String loginName,String displayName,Set<String> roleCodes,Set<String> permissionCodes,boolean mustChangePassword){this(userId,organizationId,loginName,displayName,roleCodes,permissionCodes,mustChangePassword,Set.of(),1);}
    public LoginSnapshot {
        if (organizationId <= 0) throw new IllegalArgumentException("organization context is required");
        roleCodes = Set.copyOf(roleCodes);
        roleIds = roleIds==null?Set.of():Set.copyOf(roleIds);
        permissionCodes = Set.copyOf(permissionCodes);
    }
}
