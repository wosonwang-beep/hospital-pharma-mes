package com.hospital.mes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(scanBasePackages = "com.hospital.mes", exclude = UserDetailsServiceAutoConfiguration.class)
public class MesApplication {
    public static void main(String[] args) {
        boolean bootstrap = java.util.Arrays.asList(args).contains("--mes.bootstrap-admin=true");
        SpringApplication app = new SpringApplication(MesApplication.class);
        if (bootstrap) app.setWebApplicationType(WebApplicationType.NONE);
        if (bootstrap) {
            try (var ignored = app.run(args)) {
                // One-shot operation: close infrastructure connections before returning.
            }
        } else {
            app.run(args);
        }
    }
}
