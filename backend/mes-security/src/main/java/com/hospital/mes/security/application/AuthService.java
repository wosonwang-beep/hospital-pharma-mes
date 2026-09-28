package com.hospital.mes.security.application;

import com.hospital.mes.security.identity.IdentityDirectory;
import com.hospital.mes.security.identity.LoginIdentity;
import com.hospital.mes.security.identity.LoginSnapshot;
import com.hospital.mes.security.identity.SecurityEvent;
import com.hospital.mes.security.jwt.AccessTokenCodec;
import com.hospital.mes.security.password.PasswordService;
import com.hospital.mes.security.session.SessionStore;
import java.time.Clock;
import java.time.Instant;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class AuthService {
    public record AuthResult(String accessToken, String renewalCookieValue) {}

    private final IdentityDirectory identities;
    private final PasswordService passwords;
    private final SessionStore sessions;
    private final AccessTokenCodec tokens;
    private final Clock clock;

    public AuthService(IdentityDirectory identities, PasswordService passwords,
                       SessionStore sessions, AccessTokenCodec tokens, Clock clock) {
        this.identities = identities;
        this.passwords = passwords;
        this.sessions = sessions;
        this.tokens = tokens;
        this.clock = clock;
    }

    public AuthResult login(String loginName, String plaintext, String traceId) {
        if (loginName == null || loginName.isBlank() || plaintext == null) {
            passwords.matches(plaintext, null);
            denied(null, traceId);
            throw badCredentials();
        }
        LoginIdentity identity = identities.findForLogin(loginName).orElse(null);
        Instant now = clock.instant();
        if (!passwords.canAuthenticate(identity, plaintext, now)) {
            if (identity != null && identity.enabled()
                && (identity.lockedUntil() == null || !identity.lockedUntil().isAfter(now))) {
                identities.recordLoginFailure(identity.userId(), now);
            }
            denied(identity, traceId);
            throw badCredentials();
        }
        identities.recordLoginSuccess(identity.userId(), now);
        LoginSnapshot snapshot = identities.loadLoginSnapshot(identity.userId());
        SessionStore.SessionLease lease = sessions.create(snapshot);
        try {
            identities.appendSecurityEvent(new SecurityEvent("LOGIN", "SUCCESS", identity.userId(),
                identity.userId(), null, traceId, "managed account", now));
            return new AuthResult(tokens.issue(identity.userId(), lease.sessionId()),
                lease.sessionId() + "." + lease.renewalCredential());
        } catch (RuntimeException ex) {
            sessions.revoke(lease.sessionId());
            throw ex;
        }
    }

    public AuthResult refresh(String cookieValue) {
        if (cookieValue == null) throw badCredentials();
        int separator = cookieValue.indexOf('.');
        if (separator <= 0 || separator == cookieValue.length() - 1) throw badCredentials();
        String sessionId = cookieValue.substring(0, separator);
        String credential = cookieValue.substring(separator + 1);
        SessionStore.SessionLease lease = sessions.renew(sessionId, credential)
            .orElseThrow(AuthService::badCredentials);
        return new AuthResult(tokens.issue(lease.snapshot().identity().userId(), sessionId),
            sessionId + "." + lease.renewalCredential());
    }

    public void logout(String sessionId) {
        sessions.revoke(sessionId);
    }

    public void changeOwnPassword(LoginSnapshot snapshot, String oldPassword,
                                  String newPassword, String traceId) {
        LoginIdentity current = identities.findForLogin(snapshot.loginName())
            .orElseThrow(AuthService::badCredentials);
        if (current.userId() != snapshot.userId() || !passwords.matches(oldPassword, current.passwordHash()))
            throw badCredentials();
        if (passwords.matches(newPassword, current.passwordHash()))
            throw new IllegalArgumentException("New password must differ from current password");
        String replacementHash = passwords.hash(newPassword);
        sessions.revokeAllForUser(snapshot.userId());
        if (!identities.changeOwnPassword(snapshot.userId(), current.passwordHash(),
            replacementHash, traceId, clock.instant()))
            throw new IllegalStateException("Password changed concurrently; sign in again");
    }

    private void denied(LoginIdentity identity, String traceId) {
        identities.appendSecurityEvent(new SecurityEvent("LOGIN", "DENIED", null,
            identity == null ? null : identity.userId(), null, traceId,
            "managed account", clock.instant()));
    }

    private static BadCredentialsException badCredentials() {
        return new BadCredentialsException("Invalid credentials");
    }
}
