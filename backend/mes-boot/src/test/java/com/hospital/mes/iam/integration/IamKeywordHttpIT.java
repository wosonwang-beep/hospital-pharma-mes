package com.hospital.mes.iam.integration;

import com.hospital.mes.security.identity.LoginSnapshot;
import com.hospital.mes.security.jwt.AccessTokenCodec;
import com.hospital.mes.security.session.SessionStore;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/** Regression for the real HTTP search path, not a direct service call. No DB writes. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class IamKeywordHttpIT {
    @Autowired MockMvc mvc;
    @Autowired SessionStore sessions;
    @Autowired AccessTokenCodec tokens;

    @Test void allSystemAdministrationKeywordQueriesReturnValidResponses() throws Exception {
        var lease = sessions.create(new LoginSnapshot(998877L, 1L, "admin", "Admin",
            Set.of("SYSTEM_ADMIN"), Set.of("iam:menu:view", "iam:role:view", "iam:permission:view"), false));
        try {
            var token = tokens.issue(998877L, lease.sessionId());
            for (String path : new String[]{"/api/v1/menus", "/api/v1/roles", "/api/v1/permissions"}) {
                var result = mvc.perform(get(path)
                    .param("page","0").param("size","20").param("keyword","物料主数据")
                    .header("Authorization","Bearer "+token)).andReturn();
                if (result.getResponse().getStatus() >= 500 && result.getResolvedException() != null) {
                    result.getResolvedException().printStackTrace(System.err);
                }
                assertThat(result.getResponse().getStatus())
                    .as("HTTP keyword query %s, response %s",path,result.getResponse().getContentAsString())
                    .isEqualTo(200);
            }
        } finally {
            sessions.revoke(lease.sessionId());
        }
    }
}
