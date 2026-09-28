package com.hospital.mes.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

class HmacAccessTokenCodecTest {
    private static final Instant START = Instant.parse("2026-09-28T00:00:00Z");
    private static final String KEY = Base64.getEncoder().encodeToString(
        "test-only-signing-key-with-more-than-32-bytes".getBytes(StandardCharsets.UTF_8));

    @Test
    void signedTokenCarriesOnlyIdentitySessionAndFifteenMinuteExpiry() {
        HmacAccessTokenCodec codec = codec("current", START);
        String token = codec.issue(17, "b07df146-3ec7-4cb0-9884-cc07d03c9290");
        AccessTokenCodec.TokenClaims claims = codec.verify(token);
        assertThat(claims.userId()).isEqualTo(17);
        assertThat(claims.sessionId()).isEqualTo("b07df146-3ec7-4cb0-9884-cc07d03c9290");
        assertThat(claims.expiresAt()).isEqualTo(START.plusSeconds(15 * 60));
        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
        assertThat(payload).doesNotContain("permission", "role", "menu:home");
    }

    @Test
    void alteredExpiredAndUnknownKeyIdTokensAreRejected() {
        String token = codec("current", START).issue(17, "b07df146-3ec7-4cb0-9884-cc07d03c9290");
        String[] parts = token.split("\\.");
        String signature = parts[2];
        String altered = parts[0] + "." + parts[1] + "."
            + (signature.charAt(0) == 'A' ? 'B' : 'A') + signature.substring(1);
        assertThatThrownBy(() -> codec("current", START).verify(altered))
            .isInstanceOf(BadCredentialsException.class);
        assertThatThrownBy(() -> codec("current", START.plusSeconds(15 * 60)).verify(token))
            .isInstanceOf(BadCredentialsException.class);
        assertThatThrownBy(() -> codec("other", START).verify(token))
            .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void missingOrWeakExternalKeyFailsConfiguration() {
        assertThatThrownBy(() -> new HmacAccessTokenCodec("", "", Clock.systemUTC()))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new HmacAccessTokenCodec("current", "current:YQ==", Clock.systemUTC()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private HmacAccessTokenCodec codec(String kid, Instant time) {
        return new HmacAccessTokenCodec(kid, kid + ":" + KEY,
            Clock.fixed(time, ZoneOffset.UTC));
    }
}
