package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.hospital.mes.security.identity.IdentityDirectory;
import com.hospital.mes.security.password.PasswordService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class AuthLocalCookieIT {
    @Autowired private MockMvc mvc;
    @Autowired private IdentityDirectory identities;
    @Autowired private PasswordService passwords;

    @Test
    void insecureCookieExceptionAppliesOnlyToLoopbackInLocalProfile() throws Exception {
        String login = "local" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        identities.bootstrapAdministrator(login, "Local Administrator",
            passwords.hash("Temporary passphrase 123"), Instant.now());
        String requestBody = "{\"loginName\":\"" + login
            + "\",\"password\":\"Temporary passphrase 123\"}";
        String loopback = mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(requestBody))
            .andReturn().getResponse().getHeader("Set-Cookie");
        String publicHost = mvc.perform(post("/api/v1/auth/login").with(request -> {
                request.setServerName("mes.example.org");
                return request;
            }).contentType(MediaType.APPLICATION_JSON).content(requestBody))
            .andReturn().getResponse().getHeader("Set-Cookie");
        assertThat(loopback).contains("HttpOnly", "SameSite=Strict").doesNotContain("Secure");
        assertThat(publicHost).contains("Secure");
    }
}
