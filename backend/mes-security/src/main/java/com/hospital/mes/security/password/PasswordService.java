package com.hospital.mes.security.password;

import com.hospital.mes.security.identity.LoginIdentity;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public final class PasswordService {
    private static final String VERSION = "$mes1$";
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(12);
    private static final String DUMMY_HASH = VERSION + ENCODER.encode(prehash("no-such-mes-account-password"));

    public String hash(String plaintext) {
        if (plaintext == null) {
            throw new IllegalArgumentException("Password length must be between 12 and 128 characters");
        }
        int length = plaintext.codePointCount(0, plaintext.length());
        if (length < 12 || length > 128) {
            throw new IllegalArgumentException("Password length must be between 12 and 128 characters");
        }
        return VERSION + ENCODER.encode(prehash(plaintext));
    }

    public boolean matches(String plaintext, String storedHash) {
        String hash = storedHash == null ? DUMMY_HASH : storedHash;
        boolean versioned = hash.startsWith(VERSION);
        try {
            boolean matches = ENCODER.matches(versioned ? prehash(plaintext == null ? "" : plaintext)
                : plaintext == null ? "" : plaintext,
                versioned ? hash.substring(VERSION.length()) : hash);
            return storedHash != null && matches;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean canAuthenticate(LoginIdentity identity, String plaintext, Instant now) {
        boolean matched = matches(plaintext, identity == null ? null : identity.passwordHash());
        return identity != null && identity.enabled()
            && (identity.lockedUntil() == null || !identity.lockedUntil().isAfter(now))
            && matched;
    }

    private static String prehash(String plaintext) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update("hospital-pharma-mes:password:v1\0".getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(
                digest.digest(plaintext.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}
