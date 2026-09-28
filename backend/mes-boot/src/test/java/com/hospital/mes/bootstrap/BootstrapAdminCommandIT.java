package com.hospital.mes.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hospital.mes.security.identity.IdentityDirectory;
import com.hospital.mes.security.password.PasswordService;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("ci")
@Transactional
class BootstrapAdminCommandIT {
    @Autowired private IdentityDirectory identities;
    @Autowired private PasswordService passwords;
    @Autowired private Clock clock;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void printsGeneratedSecretOnceAndRefusesAnotherAdministrator() {
        BootstrapAdminCommand command = new BootstrapAdminCommand(identities, passwords, clock);
        String login = "boot" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        command.execute(login, "First Operator", new PrintStream(output, true, StandardCharsets.UTF_8));
        String text = output.toString(StandardCharsets.UTF_8);
        String secret = text.lines().filter(line -> line.startsWith("Temporary password: "))
            .findFirst().orElseThrow().substring("Temporary password: ".length());
        assertThat(secret).hasSizeGreaterThanOrEqualTo(32);
        assertThat(passwords.matches(secret, identities.findForLogin(login).orElseThrow().passwordHash()))
            .isTrue();
        assertThat(jdbc.queryForObject("SELECT password_hash FROM sys_user WHERE login_name = ?",
            String.class, login)).doesNotContain(secret);
        assertThat(jdbc.queryForList("SELECT request_context FROM sys_security_event", String.class)
            .toString()).doesNotContain(secret);

        ByteArrayOutputStream secondOutput = new ByteArrayOutputStream();
        assertThatThrownBy(() -> command.execute("other", "Other Operator",
            new PrintStream(secondOutput, true, StandardCharsets.UTF_8)))
            .isInstanceOf(IllegalStateException.class);
        assertThat(secondOutput.toString(StandardCharsets.UTF_8)).isEmpty();
    }
}
