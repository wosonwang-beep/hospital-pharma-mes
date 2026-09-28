package com.hospital.mes.security.session;

import static org.assertj.core.api.Assertions.assertThat;

import com.hospital.mes.security.identity.LoginSnapshot;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SessionSnapshotTest {
    private final LoginSnapshot identity = new LoginSnapshot(7, "staff", "Staff",
        Set.of("ROLE_A"), Set.of("menu:home"), false);

    @Test
    void activityExtendsIdleButNeverAbsoluteLifetime() {
        Instant start = Instant.parse("2026-09-28T00:00:00Z");
        SessionSnapshot session = SessionSnapshot.start("sid", identity, start);
        assertThat(session.idleExpiresAt()).isEqualTo(start.plusSeconds(30 * 60));
        assertThat(session.absoluteExpiresAt()).isEqualTo(start.plusSeconds(8 * 60 * 60));
        SessionSnapshot nearEnd = session;
        for (int minutes = 20; minutes <= 460; minutes += 20) {
            nearEnd = nearEnd.touch(start.plusSeconds(minutes * 60L));
        }
        nearEnd = nearEnd.touch(start.plusSeconds(7 * 60 * 60 + 50 * 60));
        assertThat(nearEnd.idleExpiresAt()).isEqualTo(start.plusSeconds(8 * 60 * 60));
        assertThat(nearEnd.absoluteExpiresAt()).isEqualTo(session.absoluteExpiresAt());
    }

    @Test
    void eitherDeadlineExpiresTheSession() {
        Instant start = Instant.parse("2026-09-28T00:00:00Z");
        SessionSnapshot session = SessionSnapshot.start("sid", identity, start);
        assertThat(session.isExpired(start.plusSeconds(30 * 60))).isTrue();
        assertThat(session.isExpired(start.plusSeconds(30 * 60 - 1))).isFalse();
        SessionSnapshot later = session.touch(start.plusSeconds(10 * 60));
        assertThat(later.isExpired(start.plusSeconds(8 * 60 * 60))).isTrue();
    }
}
