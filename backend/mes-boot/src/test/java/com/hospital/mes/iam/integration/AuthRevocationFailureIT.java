package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

import com.hospital.mes.security.application.AuthService;
import com.hospital.mes.security.identity.IdentityDirectory;
import com.hospital.mes.security.password.PasswordService;
import com.hospital.mes.security.session.SessionStore;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("ci")
@Transactional
class AuthRevocationFailureIT {
    @Autowired private IdentityDirectory identities;
    @Autowired private PasswordService passwords;
    @Autowired private AuthService auth;
    @MockitoBean private SessionStore sessions;

    @Test
    void failedRedisRevocationDoesNotCommitPasswordChange() {
        String login = "revoke" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        long userId = identities.bootstrapAdministrator(login, "Revocation Test",
            passwords.hash("Temporary passphrase 123"), Instant.now());
        String originalHash = identities.findForLogin(login).orElseThrow().passwordHash();
        doThrow(new DataAccessResourceFailureException("Redis unavailable"))
            .when(sessions).revokeAllForUser(userId);

        assertThatThrownBy(() -> auth.changeOwnPassword(identities.loadLoginSnapshot(userId),
            "Temporary passphrase 123", "New passphrase 456789", "revocation-test"))
            .isInstanceOf(DataAccessResourceFailureException.class);
        assertThat(identities.findForLogin(login).orElseThrow().passwordHash()).isEqualTo(originalHash);
        assertThat(identities.loadLoginSnapshot(userId).mustChangePassword()).isTrue();
    }
}
