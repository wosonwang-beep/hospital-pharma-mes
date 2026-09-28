package com.hospital.mes.security.jwt;

import java.time.Instant;

public interface AccessTokenCodec {
    record TokenClaims(long userId, String sessionId, Instant issuedAt,
                       Instant expiresAt, String keyId) {
    }

    String issue(long userId, String sessionId);
    TokenClaims verify(String token);
}
