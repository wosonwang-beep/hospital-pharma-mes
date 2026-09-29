package com.hospital.mes.audit.application;

import java.util.Set;

public interface PlatformPrincipal {
    long userId();
    long organizationId();
    Set<String> roleCodes();
    Set<String> permissionCodes();
}
