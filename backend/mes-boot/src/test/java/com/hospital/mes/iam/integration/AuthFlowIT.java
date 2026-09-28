package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.security.identity.IdentityDirectory;
import com.hospital.mes.security.jwt.AccessTokenCodec;
import com.hospital.mes.security.password.PasswordService;
import com.hospital.mes.security.session.SessionStore;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
@Transactional
class AuthFlowIT {
    private static final String TEMPORARY_PASSWORD = "Temporary passphrase 123";
    private static final String NEW_PASSWORD = "New passphrase 456789";

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private IdentityDirectory identities;
    @Autowired private PasswordService passwords;
    @Autowired private AccessTokenCodec tokens;
    @Autowired private SessionStore sessions;
    @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbc;
    @Autowired private StringRedisTemplate redis;
    private final List<String> sessionIds = new ArrayList<>();

    @AfterEach void removeSessions() { sessionIds.forEach(sessions::revoke); }

    @Test
    void bootstrapIsOneTimeAndInitialLoginRequiresPasswordChange() throws Exception {
        String login = bootstrap();
        assertThatThrownBy(() -> identities.bootstrapAdministrator("another", "Another",
            passwords.hash(TEMPORARY_PASSWORD), Instant.now()))
            .isInstanceOf(IllegalStateException.class);

        MockHttpServletResponse response = login(login, TEMPORARY_PASSWORD);
        assertThat(response.getStatus()).isEqualTo(200);
        String cookie = response.getHeader("Set-Cookie");
        assertThat(cookie).contains("MES_RENEWAL=", "HttpOnly", "Secure", "SameSite=Strict",
            "Path=/api/v1/auth");
        String token = accessToken(response);
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.mustChangePassword").value(true))
            .andExpect(jsonPath("$.data.roleCodes[0]").value("SYSTEM_ADMIN"));
        mvc.perform(get("/api/v1/foundation/status").header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_security_event WHERE event_type='LOGIN' AND outcome='SUCCESS'",
            Integer.class)).isGreaterThanOrEqualTo(1);
    }

    @Test
    void bootstrapRemainsOneTimeAfterOriginalAdminAssignmentIsRevoked() {
        String login = bootstrap();
        jdbc.update("UPDATE sys_user_role SET revoked_at = CURRENT_TIMESTAMP(6) WHERE user_id = "
            + "(SELECT id FROM sys_user WHERE login_name = ?)", login);
        assertThatThrownBy(() -> identities.bootstrapAdministrator("replacement", "Replacement",
            passwords.hash(TEMPORARY_PASSWORD), Instant.now()))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void passwordChangeRevokesOldSessionAndNewLoginNoLongerNeedsChange() throws Exception {
        String login = bootstrap();
        String oldToken = accessToken(login(login, TEMPORARY_PASSWORD));
        mvc.perform(post("/api/v1/auth/change-password")
                .header("Authorization", "Bearer " + oldToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(new PasswordChange(TEMPORARY_PASSWORD, NEW_PASSWORD))))
            .andExpect(status().isOk());
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + oldToken))
            .andExpect(status().isUnauthorized());
        MockHttpServletResponse fresh = login(login, NEW_PASSWORD);
        mvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + accessToken(fresh)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.mustChangePassword").value(false));
    }

    @Test
    void renewalRotatesCookieAndLogoutRevokesAccess() throws Exception {
        String login = bootstrap();
        MockHttpServletResponse first = login(login, TEMPORARY_PASSWORD);
        String oldCookie = cookieValue(first);
        MockHttpServletResponse renewed = mvc.perform(post("/api/v1/auth/refresh")
                .header("Origin", "http://localhost")
                .cookie(new Cookie("MES_RENEWAL", oldCookie)))
            .andExpect(status().isOk())
            .andReturn().getResponse();
        assertThat(cookieValue(renewed)).isNotEqualTo(oldCookie);
        mvc.perform(post("/api/v1/auth/refresh").header("Origin", "http://localhost")
                .cookie(new Cookie("MES_RENEWAL", oldCookie)))
            .andExpect(status().isUnauthorized());
        String renewedToken = accessToken(renewed);
        mvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + renewedToken))
            .andExpect(status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                .string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + renewedToken))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void failedLoginIsNonEnumeratingAndRecordsOutcomeWithoutSecret() throws Exception {
        String login = bootstrap();
        MockHttpServletResponse unknown = login("missing-" + UUID.randomUUID(), "wrong passphrase");
        MockHttpServletResponse wrong = login(login, "wrong passphrase");
        assertThat(unknown.getStatus()).isEqualTo(401);
        assertThat(wrong.getStatus()).isEqualTo(401);
        assertThat(json.readTree(unknown.getContentAsString()).path("message").asText())
            .isEqualTo(json.readTree(wrong.getContentAsString()).path("message").asText());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_security_event WHERE event_type='LOGIN' AND outcome='DENIED'",
            Integer.class)).isGreaterThanOrEqualTo(2);
        assertThat(jdbc.queryForList("SELECT request_context FROM sys_security_event WHERE event_type='LOGIN'", String.class)
            .toString()).doesNotContain("wrong passphrase", TEMPORARY_PASSWORD);
    }

    @Test
    void refreshRejectsCrossOriginRequestWithoutConsumingCredential() throws Exception {
        String login = bootstrap();
        MockHttpServletResponse first = login(login, TEMPORARY_PASSWORD);
        String value = cookieValue(first);
        mvc.perform(post("/api/v1/auth/refresh").header("Origin", "https://other.example")
                .cookie(new Cookie("MES_RENEWAL", value)))
            .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/refresh").header("Origin", "http://localhost")
                .cookie(new Cookie("MES_RENEWAL", value)))
            .andExpect(status().isOk());
    }

    @Test
    void refreshIgnoresExpiredBearerAndUsesOnlyCookieCredential() throws Exception {
        String login = bootstrap();
        MockHttpServletResponse first = login(login, TEMPORARY_PASSWORD);
        mvc.perform(post("/api/v1/auth/refresh").header("Origin", "http://localhost")
                .header("Authorization", "Bearer expired-or-altered-token")
                .cookie(new Cookie("MES_RENEWAL", cookieValue(first))))
            .andExpect(status().isOk());
    }

    @Test
    void malformedAuthenticationBodiesAreClientErrors() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        String login = bootstrap();
        String token = accessToken(login(login, TEMPORARY_PASSWORD));
        mvc.perform(post("/api/v1/auth/change-password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void disableAndRoleChangesApplyOnlyOnNextLogin() throws Exception {
        String login = bootstrap();
        String first = accessToken(login(login, TEMPORARY_PASSWORD));
        jdbc.update("UPDATE sys_role SET enabled = FALSE WHERE role_code = 'SYSTEM_ADMIN'");
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + first))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.roleCodes[0]").value("SYSTEM_ADMIN"));
        String second = accessToken(login(login, TEMPORARY_PASSWORD));
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + second))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.roleCodes.length()").value(0));
        jdbc.update("UPDATE sys_user SET enabled = FALSE WHERE login_name = ?", login);
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + second))
            .andExpect(status().isOk());
        assertThat(login(login, TEMPORARY_PASSWORD).getStatus()).isEqualTo(401);
    }

    @Test
    void expiredRedisDeadlinesInvalidateOtherwiseValidBearer() throws Exception {
        String login = bootstrap();
        String first = accessToken(login(login, TEMPORARY_PASSWORD));
        String firstId = tokens.verify(first).sessionId();
        redis.opsForHash().put("mes:iam:session:" + firstId, "idleExpiresAt", "1");
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + first))
            .andExpect(status().isUnauthorized());
        String second = accessToken(login(login, TEMPORARY_PASSWORD));
        String secondId = tokens.verify(second).sessionId();
        redis.opsForHash().put("mes:iam:session:" + secondId, "absoluteExpiresAt", "1");
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + second))
            .andExpect(status().isUnauthorized());
    }

    private String bootstrap() {
        String login = "admin" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        identities.bootstrapAdministrator(login, "Test Administrator",
            passwords.hash(TEMPORARY_PASSWORD), Instant.now());
        return login;
    }

    private MockHttpServletResponse login(String login, String password) throws Exception {
        MockHttpServletResponse response = mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(new Credentials(login, password))))
            .andReturn().getResponse();
        if (response.getStatus() == 200) {
            sessionIds.add(tokens.verify(accessToken(response)).sessionId());
        }
        return response;
    }

    private String accessToken(MockHttpServletResponse response) throws Exception {
        return json.readTree(response.getContentAsString()).path("data").path("accessToken").asText();
    }

    private String cookieValue(MockHttpServletResponse response) {
        return response.getHeader("Set-Cookie").split(";", 2)[0].split("=", 2)[1];
    }

    private record Credentials(String loginName, String password) {}
    private record PasswordChange(String oldPassword, String newPassword) {}
}
