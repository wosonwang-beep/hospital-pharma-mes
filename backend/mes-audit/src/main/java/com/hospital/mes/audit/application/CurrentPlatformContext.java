package com.hospital.mes.audit.application;

import java.util.Set;

public record CurrentPlatformContext(long organizationId, long actorId, Set<String> roleCodes,
                                     Set<String> permissionCodes, String sessionId, String requestId, Set<String> roleIds, int identityFormatVersion) {
    public CurrentPlatformContext(long organizationId,long actorId,Set<String> roleCodes,Set<String> permissionCodes,String sessionId,String requestId){this(organizationId,actorId,roleCodes,permissionCodes,sessionId,requestId,Set.of(),1);}
    public CurrentPlatformContext {
        if (organizationId <= 0 || actorId <= 0 || sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("complete authenticated platform context is required");
        }
        roleCodes = Set.copyOf(roleCodes);
        roleIds = roleIds==null?Set.of():Set.copyOf(roleIds);
        permissionCodes = Set.copyOf(permissionCodes);
    }

    public String roleSnapshot() { return roleCodes.stream().sorted().findFirst().orElse(null); }
    public boolean hasPermission(String permission) { return permissionCodes.contains(permission); }
}
