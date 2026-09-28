package com.hospital.mes.security.session;

import com.hospital.mes.security.identity.LoginSnapshot;
import java.time.Instant;

public record SessionSnapshot(String sessionId, LoginSnapshot identity,
                              Instant createdAt, Instant absoluteExpiresAt,
                              Instant idleExpiresAt) {
    public static SessionSnapshot start(String sessionId, LoginSnapshot identity, Instant now) {
        return new SessionSnapshot(sessionId, identity, now, now.plusSeconds(8 * 60 * 60),
            now.plusSeconds(30 * 60));
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(absoluteExpiresAt) || !now.isBefore(idleExpiresAt);
    }

    public SessionSnapshot touch(Instant now) {
        if (isExpired(now)) throw new IllegalStateException("Session expired");
        Instant nextIdle = now.plusSeconds(30 * 60);
        if (nextIdle.isAfter(absoluteExpiresAt)) nextIdle = absoluteExpiresAt;
        return new SessionSnapshot(sessionId, identity, createdAt, absoluteExpiresAt, nextIdle);
    }
}
