package com.hospital.mes.security.session;

import com.hospital.mes.security.identity.LoginSnapshot;
import java.util.Optional;

public interface SessionStore {
    record SessionLease(String sessionId, String renewalCredential, SessionSnapshot snapshot) {
    }

    SessionLease create(LoginSnapshot identity);
    Optional<SessionSnapshot> findById(String sessionId);
    Optional<SessionSnapshot> touch(String sessionId);
    Optional<SessionLease> renew(String sessionId, String renewalCredential);
    void revoke(String sessionId);
    void revokeAllForUser(long userId);
}
