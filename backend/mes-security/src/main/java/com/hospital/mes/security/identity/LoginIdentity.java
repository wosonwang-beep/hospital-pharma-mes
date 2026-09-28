package com.hospital.mes.security.identity;

import java.time.Instant;

public record LoginIdentity(long userId, String loginName, String displayName,
                            String passwordHash, boolean enabled, int failedLoginCount,
                            Instant lockedUntil, boolean mustChangePassword, long version) {
}
