package com.hospital.mes.bootstrap;

import com.hospital.mes.security.identity.IdentityDirectory;
import com.hospital.mes.security.password.PasswordService;
import java.io.PrintStream;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnNotWebApplication
@ConditionalOnProperty(prefix = "mes", name = "bootstrap-admin", havingValue = "true")
public final class BootstrapAdminCommand implements ApplicationRunner {
    private final IdentityDirectory identities;
    private final PasswordService passwords;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public BootstrapAdminCommand(IdentityDirectory identities, PasswordService passwords, Clock clock) {
        this.identities = identities;
        this.passwords = passwords;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (args.getOptionNames().stream().anyMatch(name -> name.toLowerCase().contains("password")
            || name.toLowerCase().contains("secret"))) {
            throw new IllegalArgumentException("Bootstrap accepts no password or secret option");
        }
        execute(singleOption(args, "mes.bootstrap-login"),
            singleOption(args, "mes.bootstrap-display"), System.out);
    }

    public void execute(String loginName, String displayName, PrintStream output) {
        if (loginName == null || loginName.isBlank() || displayName == null || displayName.isBlank())
            throw new IllegalArgumentException("Bootstrap login and display name are required");
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String temporary = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        identities.bootstrapAdministrator(loginName, displayName, passwords.hash(temporary), clock.instant());
        output.println("Administrator created: " + loginName);
        output.println("Temporary password: " + temporary);
        output.println("Save it securely now; it cannot be displayed again and must be changed at first login.");
    }

    private static String singleOption(ApplicationArguments args, String name) {
        var values = args.getOptionValues(name);
        if (values == null || values.size() != 1 || values.get(0).isBlank())
            throw new IllegalArgumentException("Exactly one --" + name + " value is required");
        return values.get(0);
    }
}
