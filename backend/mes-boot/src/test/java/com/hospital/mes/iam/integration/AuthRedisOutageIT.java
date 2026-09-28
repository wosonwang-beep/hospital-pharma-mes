package com.hospital.mes.iam.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hospital.mes.security.identity.IdentityDirectory;
import com.hospital.mes.security.jwt.AccessTokenCodec;
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

@SpringBootTest(properties = "spring.data.redis.port=6399")
@AutoConfigureMockMvc
@ActiveProfiles("ci")
@Transactional
class AuthRedisOutageIT {
    @Autowired private MockMvc mvc;
    @Autowired private IdentityDirectory identities;
    @Autowired private PasswordService passwords;
    @Autowired private AccessTokenCodec tokens;

    @Test
    void unavailableRedisCannotCreateRenewOrAuthenticateSession() throws Exception {
        String login = "outage" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        identities.bootstrapAdministrator(login, "Outage Test",
            passwords.hash("Temporary passphrase 123"), Instant.now());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginName\":\"" + login
                    + "\",\"password\":\"Temporary passphrase 123\"}"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.code").value("DEPENDENCY_UNAVAILABLE"));
        mvc.perform(post("/api/v1/auth/refresh").header("Origin", "http://localhost")
                .cookie(new jakarta.servlet.http.Cookie("MES_RENEWAL",
                    UUID.randomUUID() + ".opaque-renewal-secret")))
            .andExpect(status().isServiceUnavailable());
        mvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + tokens.issue(1000, UUID.randomUUID().toString())))
            .andExpect(status().isUnauthorized());
    }
}
