package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;

import com.hospital.mes.security.application.AuthService;
import com.hospital.mes.security.identity.IdentityDirectory;
import com.hospital.mes.security.jwt.AccessTokenCodec;
import com.hospital.mes.security.password.PasswordService;
import com.hospital.mes.security.session.SessionStore;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@ActiveProfiles("ci")
class AuthPasswordRaceIT {
    @Autowired private AuthService auth;
    @Autowired private IdentityDirectory identities;
    @Autowired private PasswordService passwords;
    @Autowired private AccessTokenCodec tokens;
    @Autowired private JdbcTemplate jdbc;
    @MockitoSpyBean private SessionStore sessions;
    private Long userId;

    @AfterEach void cleanup() {
        if (userId == null) return;
        sessions.revokeAllForUser(userId);
        jdbc.update("DELETE FROM sys_security_event WHERE actor_user_id = ? OR target_user_id = ?",
            userId, userId);
        jdbc.update("DELETE FROM sys_user_role WHERE user_id = ?", userId);
        jdbc.update("DELETE FROM sys_user WHERE id = ?", userId);
    }

    @Test
    void concurrentOldPasswordLoginCannotOutlivePasswordChange() throws Exception {
        String login = "race" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        userId = identities.bootstrapAdministrator(login, "Race Test",
            passwords.hash("Temporary passphrase 123"), Instant.now());
        var snapshot = identities.loadLoginSnapshot(userId);
        CountDownLatch revoked = new CountDownLatch(1);
        CountDownLatch allowChange = new CountDownLatch(1);
        doAnswer(call -> {
            call.callRealMethod();
            revoked.countDown();
            if (!allowChange.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Test timeout");
            return null;
        }).when(sessions).revokeAllForUser(userId);

        try (var pool = Executors.newVirtualThreadPerTaskExecutor()) {
            var change = pool.submit(() -> auth.changeOwnPassword(snapshot,
                "Temporary passphrase 123", "New passphrase 456789", "race-test"));
            assertThat(revoked.await(10, TimeUnit.SECONDS)).isTrue();
            var concurrentLogin = pool.submit(() -> auth.login(login,
                "Temporary passphrase 123", "race-login"));
            AuthService.AuthResult early = null;
            try { early = concurrentLogin.get(300, TimeUnit.MILLISECONDS); }
            catch (java.util.concurrent.TimeoutException expected) { /* row-lock serialization */ }
            finally { allowChange.countDown(); }
            change.get(10, TimeUnit.SECONDS);
            if (early != null) {
                assertThat(sessions.findById(tokens.verify(early.accessToken()).sessionId())).isEmpty();
            } else {
                try {
                    concurrentLogin.get(10, TimeUnit.SECONDS);
                    throw new AssertionError("Old password login succeeded after password change");
                } catch (ExecutionException expected) {
                    assertThat(expected.getCause()).isInstanceOf(BadCredentialsException.class);
                }
            }
        }
    }
}
