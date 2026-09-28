package com.hospital.mes.security.password;

import com.hospital.mes.security.identity.LoginIdentity;
import java.time.Instant;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public final class PasswordService {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(12);
    private static final String DUMMY_HASH = ENCODER.encode("no-such-mes-account-password");

    public String hash(String plaintext) {
        if (plaintext == null) {
            throw new IllegalArgumentException("Password length must be between 12 and 128 characters");
        }
        int length = plaintext.codePointCount(0, plaintext.length());
        if (length < 12 || length > 128) {
            throw new IllegalArgumentException("Password length must be between 12 and 128 characters");
        }
        return ENCODER.encode(plaintext);
    }

    public boolean matches(String plaintext, String storedHash) {
        boolean matches = ENCODER.matches(plaintext == null ? "" : plaintext,
            storedHash == null ? DUMMY_HASH : storedHash);
        return storedHash != null && matches;
    }

    public boolean canAuthenticate(LoginIdentity identity, String plaintext, Instant now) {
        boolean matched = matches(plaintext, identity == null ? null : identity.passwordHash());
        return identity != null && identity.enabled()
            && (identity.lockedUntil() == null || !identity.lockedUntil().isAfter(now))
            && matched;
    }
}
