package com.hospital.mes.security.identity;

import java.time.Instant;
import java.util.Optional;

public interface IdentityDirectory {
    Optional<LoginIdentity> findForLogin(String loginName);
    void recordLoginFailure(long userId, Instant now);
    void recordLoginSuccess(long userId, Instant now);
    LoginSnapshot loadLoginSnapshot(long userId);
    void appendSecurityEvent(SecurityEvent event);
}
