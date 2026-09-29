package com.hospital.mes.iam.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hospital.mes.security.application.AuthService;
import com.hospital.mes.security.identity.LoginSnapshot;
import com.hospital.mes.security.jwt.AccessTokenCodec;
import com.hospital.mes.security.password.PasswordService;
import com.hospital.mes.security.session.SessionStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
@Transactional
class AdminSessionSnapshotIT {
    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PasswordService passwords;
    @Autowired private AuthService auth;
    @Autowired private SessionStore sessions;
    @Autowired private AccessTokenCodec tokens;
    private final List<String> leases = new ArrayList<>();

    @AfterEach void cleanup() { leases.forEach(sessions::revoke); }

    @Test
    void routineRoleGrantChangesPermissionsOnlyAfterNextLogin() throws Exception {
        String login = "staff" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String password = "Temporary passphrase 123";
        jdbc.update("INSERT INTO sys_user (login_name, login_name_normalized, display_name, password_hash, must_change_password) VALUES (?, ?, 'Staff', ?, FALSE)",
            login, login, passwords.hash(password));
        long userId = jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name = ?", Long.class, login);
        long roleId = jdbc.queryForObject("SELECT id FROM sys_role WHERE role_code = 'SYSTEM_ADMIN'", Long.class);
        String first = auth.login(login, password, "snapshot-test").accessToken();
        leases.add(tokens.verify(first).sessionId());
        mvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(first)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.permissionCodes.length()").value(0));
        mvc.perform(post("/api/v1/admin/users/{userId}/roles/{roleId}", userId, roleId)
                .header("Authorization", bearer(administrator())))
            .andExpect(status().isOk());
        mvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(first)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.permissionCodes.length()").value(0));
        String second = auth.login(login, password, "snapshot-test").accessToken();
        leases.add(tokens.verify(second).sessionId());
        mvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(second)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.permissionCodes[?(@=='menu:iam:users')]").isNotEmpty());
    }

    private String administrator() {
        var lease = sessions.create(new LoginSnapshot(998877L, 1L, "admin", "Admin", Set.of("SYSTEM_ADMIN"),
            Set.of("menu:iam:users", "action:iam:user.manage"), false));
        leases.add(lease.sessionId());
        return tokens.issue(998877L, lease.sessionId());
    }
    private static String bearer(String token) { return "Bearer " + token; }
}
