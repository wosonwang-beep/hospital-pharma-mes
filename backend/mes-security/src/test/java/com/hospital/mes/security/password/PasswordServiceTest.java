package com.hospital.mes.security.password;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hospital.mes.security.identity.LoginIdentity;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordServiceTest {
    private final PasswordService passwords = new PasswordService();

    @Test
    void hashesUnicodeAndSpacesWithBcryptCostAtLeastTwelve() {
        String password = "院内 员工 passphrase";
        String hash = passwords.hash(password);
        assertThat(hash).startsWith("$mes1$$2");
        assertThat(Integer.parseInt(hash.substring(10, 12))).isGreaterThanOrEqualTo(12);
        assertThat(passwords.matches(password, hash)).isTrue();
        assertThat(passwords.matches("wrong password", hash)).isFalse();
    }

    @Test
    void rejectsPasswordsOutsideTwelveToOneHundredTwentyEightCharacters() {
        assertThatThrownBy(() -> passwords.hash("12345678901")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> passwords.hash("x".repeat(129))).isInstanceOf(IllegalArgumentException.class);
        assertThat(passwords.matches("any password", null)).isFalse();
    }

    @Test
    void deniedAccountsAndUnknownNamesHaveTheSamePublicResult() {
        String hash = passwords.hash("valid passphrase");
        Instant now = Instant.parse("2026-09-28T00:00:00Z");
        LoginIdentity enabled = new LoginIdentity(1, "staff", "Staff", hash, true, 0,
            null, false, 0);
        LoginIdentity disabled = new LoginIdentity(2, "disabled", "Disabled", hash, false, 0,
            null, false, 0);
        LoginIdentity locked = new LoginIdentity(3, "locked", "Locked", hash, true, 5,
            now.plusSeconds(60), false, 0);
        assertThat(passwords.canAuthenticate(enabled, "valid passphrase", now)).isTrue();
        assertThat(passwords.canAuthenticate(disabled, "valid passphrase", now)).isFalse();
        assertThat(passwords.canAuthenticate(locked, "valid passphrase", now)).isFalse();
        assertThat(passwords.canAuthenticate(null, "valid passphrase", now)).isFalse();
    }

    @Test
    void suffixBeyondBcryptByteBoundaryStillChangesCredential() {
        String ascii = "a".repeat(72) + "A";
        String unicode = "院".repeat(24) + "甲";
        assertThat(passwords.matches("a".repeat(72) + "B", passwords.hash(ascii))).isFalse();
        assertThat(passwords.matches("院".repeat(24) + "乙", passwords.hash(unicode))).isFalse();
    }

    @Test
    void acceptsFullLengthUnicodeAndExistingUnversionedHash() {
        String longUnicode = "院".repeat(128);
        assertThat(passwords.matches(longUnicode, passwords.hash(longUnicode))).isTrue();
        String legacy = new BCryptPasswordEncoder(12).encode("Existing passphrase 123");
        assertThat(passwords.matches("Existing passphrase 123", legacy)).isTrue();
    }
}
