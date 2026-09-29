package com.hospital.mes.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.common.exception.ResourceConflictException;
import com.hospital.mes.integration.application.InboxApplicationService;
import com.hospital.mes.integration.application.IntegrationClaimService;
import java.util.Set;
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
class Mes001IntegrationMessageIT {
    @Autowired private InboxApplicationService inbox;
    @Autowired private IntegrationClaimService claims;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void duplicateInboxDeliveryReturnsTheExistingRecord() {
        String messageId = "dedupe-" + UUID.randomUUID();

        var first = inbox.receive(11, 5, "LIMS", messageId, "{\"result\":\"PASS\"}");
        var duplicate = inbox.receive(11, 5, "LIMS", messageId, "{\"result\":\"PASS\"}");

        assertThat(duplicate.id()).isEqualTo(first.id());
        assertThat(jdbc.queryForObject("""
            SELECT COUNT(*) FROM integration_inbox
             WHERE org_id = ? AND source_system = ? AND message_id = ?
            """, Integer.class, 11, "LIMS", messageId)).isEqualTo(1);
    }

    @Test
    void conditionalClaimHasOneWinnerAndRecordsTheTechnicalActor() {
        String messageId = "claim-" + UUID.randomUUID();
        var received = inbox.receive(11, 5, "LIMS", messageId, "{}");
        CurrentPlatformContext technicalContext = new CurrentPlatformContext(
            11, 7, Set.of(), Set.of(), "worker-session", null);

        var claimed = claims.claimInbox(technicalContext, received.id(), received.versionNo());

        assertThat(claimed.versionNo()).isEqualTo(received.versionNo() + 1);
        assertThat(jdbc.queryForObject("SELECT updated_by FROM integration_inbox WHERE id = ?",
            Long.class, received.id())).isEqualTo(7);
        assertThatThrownBy(() -> claims.claimInbox(technicalContext, received.id(), received.versionNo()))
            .isInstanceOf(ResourceConflictException.class);
    }
}
