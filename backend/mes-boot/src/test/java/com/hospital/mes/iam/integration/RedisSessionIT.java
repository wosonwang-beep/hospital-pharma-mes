package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.hospital.mes.security.identity.LoginSnapshot;
import com.hospital.mes.security.session.SessionStore;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("ci")
class RedisSessionIT {
    private static final Instant START = Instant.parse("2030-01-01T00:00:00Z");

    @TestConfiguration
    static class ClockConfig {
        @Bean @Primary MutableClock mutableClock() { return new MutableClock(); }
    }

    static final class MutableClock extends Clock {
        private Instant current = START;
        void set(Instant instant) { current = instant; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return current; }
    }

    @Autowired private SessionStore sessions;
    @Autowired private StringRedisTemplate redis;
    @Autowired private MutableClock clock;
    private String sessionId;
    private long userId;

    @BeforeEach void resetClock() { clock.set(START); }
    @AfterEach void cleanup() { if (sessionId != null) sessions.revoke(sessionId); }

    @Test
    void persistsSnapshotButOnlyCredentialDigestAndRotatesOnce() {
        userId = System.nanoTime();
        SessionStore.SessionLease issued = sessions.create(identity(userId));
        sessionId = issued.sessionId();
        assertThat(sessions.findById(sessionId).orElseThrow().identity().permissionCodes())
            .containsExactly("menu:home");
        assertThat(redis.opsForHash().entries("mes:iam:session:" + sessionId).values().toString())
            .doesNotContain(issued.renewalCredential());

        SessionStore.SessionLease renewed = sessions.renew(sessionId, issued.renewalCredential()).orElseThrow();
        assertThat(renewed.renewalCredential()).isNotEqualTo(issued.renewalCredential());
        assertThat(sessions.renew(sessionId, issued.renewalCredential())).isEmpty();
        assertThat(sessions.renew(sessionId, renewed.renewalCredential())).isPresent();
    }

    @Test
    void idleAndAbsoluteDeadlinesAreEnforcedWithoutDatabaseReload() {
        userId = System.nanoTime();
        SessionStore.SessionLease issued = sessions.create(identity(userId));
        sessionId = issued.sessionId();
        clock.set(START.plusSeconds(29 * 60));
        assertThat(sessions.touch(sessionId)).isPresent();
        clock.set(START.plusSeconds(60 * 60));
        assertThat(sessions.findById(sessionId)).isEmpty();
        sessions.revoke(sessionId);
        clock.set(START);
        sessionId = sessions.create(identity(userId)).sessionId();
        for (int minutes = 20; minutes <= 460; minutes += 20) {
            clock.set(START.plusSeconds(minutes * 60L));
            assertThat(sessions.touch(sessionId)).isPresent();
        }
        clock.set(START.plusSeconds(8 * 60 * 60));
        assertThat(sessions.findById(sessionId)).isEmpty();
    }

    @Test
    void concurrentRenewalAllowsOnlyOneWinner() throws Exception {
        userId = System.nanoTime();
        SessionStore.SessionLease issued = sessions.create(identity(userId));
        sessionId = issued.sessionId();
        CountDownLatch start = new CountDownLatch(1);
        try (var workers = Executors.newFixedThreadPool(2)) {
            var first = workers.submit(() -> {
                start.await();
                return sessions.renew(sessionId, issued.renewalCredential()).isPresent();
            });
            var second = workers.submit(() -> {
                start.await();
                return sessions.renew(sessionId, issued.renewalCredential()).isPresent();
            });
            start.countDown();
            assertThat((first.get(5, TimeUnit.SECONDS) ? 1 : 0)
                + (second.get(5, TimeUnit.SECONDS) ? 1 : 0)).isEqualTo(1);
        }
    }

    @Test
    void revokeAllInvalidatesEverySessionForUser() {
        userId = System.nanoTime();
        String first = sessions.create(identity(userId)).sessionId();
        sessionId = sessions.create(identity(userId)).sessionId();
        sessions.revokeAllForUser(userId);
        assertThat(sessions.findById(first)).isEmpty();
        assertThat(sessions.findById(sessionId)).isEmpty();
    }

    private LoginSnapshot identity(long id) {
        return new LoginSnapshot(id, 1L, "staff", "Staff", Set.of("ROLE_A"), Set.of("menu:home"), false);
    }
}
