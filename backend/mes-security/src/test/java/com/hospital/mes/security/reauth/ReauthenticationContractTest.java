package com.hospital.mes.security.reauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.signature.ConsumedReauthentication;
import com.hospital.mes.audit.signature.ExpectedReauthenticationBinding;
import com.hospital.mes.audit.signature.ReauthenticationRequest;
import com.hospital.mes.audit.signature.SignatureMeaning;
import com.hospital.mes.security.identity.IdentityDirectory;
import com.hospital.mes.security.identity.LoginIdentity;
import com.hospital.mes.security.password.PasswordService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ReauthenticationContractTest {
    private static final Instant NOW = Instant.parse("2026-09-29T00:00:00Z");

    @Test
    void tokenIsExactlyFiveMinutesSingleUseAndBoundToAllSevenValues() {
        PasswordService passwords = new PasswordService();
        String hash = passwords.hash("ValidPassword!123");
        IdentityDirectory identities = new IdentityDirectoryStub(hash);
        MemoryTokenStore tokens = new MemoryTokenStore();
        ReauthenticationService service = new ReauthenticationService(identities, passwords, tokens,
            Clock.fixed(NOW, ZoneOffset.UTC));
        CurrentPlatformContext context = new CurrentPlatformContext(11, 7, Set.of("QA"), Set.of("ebr:sign"), "session-1", "req-1");
        var challenge = service.issue(new ReauthenticationRequest("TEST_RECORD", "42", SignatureMeaning.VERIFY,
            3, "ValidPassword!123"), context);
        assertThat(challenge.expiresAt()).isEqualTo(NOW.plusSeconds(300));
        ExpectedReauthenticationBinding expected = new ExpectedReauthenticationBinding(7, "session-1", 11,
            "TEST_RECORD", "42", SignatureMeaning.VERIFY, 3);
        ConsumedReauthentication consumed = service.consume(challenge.token(), expected);
        assertThat(consumed.reauthenticatedAt()).isEqualTo(NOW);
        assertThatThrownBy(() -> service.consume(challenge.token(), expected))
            .isInstanceOf(ReauthenticationFailedException.class);
    }

    private static final class MemoryTokenStore implements ReauthenticationTokenStore {
        private final Map<String, StoredReauthentication> values = new HashMap<>();
        public void put(String token, StoredReauthentication value) { values.put(token, value); }
        public StoredReauthentication consume(String token) { return values.remove(token); }
    }

    private static final class IdentityDirectoryStub implements IdentityDirectory {
        private final String hash; IdentityDirectoryStub(String hash) { this.hash = hash; }
        public Optional<LoginIdentity> findForLogin(String name) { return Optional.of(new LoginIdentity(7, "user", "User", hash, true, 0, null, false, 0)); }
        public Optional<LoginIdentity> findForLoginForUpdate(String n){return findForLogin(n);} public void recordLoginFailure(long a, Instant b){}
        public void recordLoginSuccess(long a, Instant b){} public com.hospital.mes.security.identity.LoginSnapshot loadLoginSnapshot(long a){return new com.hospital.mes.security.identity.LoginSnapshot(7,11,"user","User",Set.of("QA"),Set.of("ebr:sign"),false);}
        public void appendSecurityEvent(com.hospital.mes.security.identity.SecurityEvent e){} public long bootstrapAdministrator(String a,String b,String c,Instant d){return 0;}
        public boolean changeOwnPassword(long a,String b,String c,String d,Instant e){return false;}
    }
}
