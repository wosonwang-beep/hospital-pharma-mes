package com.hospital.mes.integration.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hospital.mes.audit.application.AuditApplicationService;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.domain.AuditCommand;
import com.hospital.mes.integration.domain.InboxMessage;
import com.hospital.mes.integration.domain.InboxStatus;
import com.hospital.mes.integration.policy.ControlledIntegrationRetryPolicy;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class IntegrationClaimServiceTest {
    private static final Instant NOW = Instant.parse("2026-09-29T00:15:00Z");
    private static final CurrentPlatformContext TECHNICAL_CONTEXT = new CurrentPlatformContext(
        11, 7, Set.of(), Set.of(), "worker-session", null);

    @Test
    void recoversAnAbandonedClaimThroughTheControlledPolicy() {
        IntegrationMessageStore store = mock(IntegrationMessageStore.class);
        AuditApplicationService audit = mock(AuditApplicationService.class);
        InboxMessage claimed = InboxMessage.received(
            11, 5, "LIMS", "m1", "{}", Instant.parse("2026-09-29T00:00:00Z"))
            .claim(Instant.parse("2026-09-29T00:00:00Z"));
        when(store.findInbox(11, 42)).thenReturn(withId(claimed, 42));
        when(store.saveInbox(any(), eq(1L), eq(7L), eq(NOW))).thenReturn(true);
        IntegrationClaimService service = service(store, audit);

        InboxMessage recovered = service.recoverAbandonedInbox(TECHNICAL_CONTEXT, 42, 1);

        assertThat(recovered.status()).isEqualTo(InboxStatus.RETRY_WAIT);
        assertThat(recovered.retryCount()).isEqualTo(1);
        verify(store).saveInbox(any(), eq(1L), eq(7L), eq(NOW));
    }

    @Test
    void eighthFailureAppendsDeadLetterAuditWithTheTechnicalActor() {
        IntegrationMessageStore store = mock(IntegrationMessageStore.class);
        AuditApplicationService audit = mock(AuditApplicationService.class);
        InboxMessage claimed = new InboxMessage(42, 11, 5, "LIMS", "m1", "{}",
            InboxStatus.PROCESSING, Instant.parse("2026-09-28T00:00:00Z"), null, 7, null,
            "TIMEOUT", "failed", 10, NOW.minusSeconds(30));
        when(store.saveInbox(any(), eq(10L), eq(7L), eq(NOW))).thenReturn(true);
        IntegrationClaimService service = service(store, audit);

        InboxMessage failed = service.failInbox(TECHNICAL_CONTEXT, claimed, "TIMEOUT", "failed");

        assertThat(failed.status()).isEqualTo(InboxStatus.DEAD_LETTER);
        ArgumentCaptor<AuditCommand> event = ArgumentCaptor.forClass(AuditCommand.class);
        verify(audit).append(event.capture());
        assertThat(event.getValue().action()).isEqualTo("INTEGRATION_MESSAGE_DEAD_LETTERED");
        assertThat(event.getValue().objectId()).isEqualTo("INBOX:42");
        assertThat(event.getValue().actorId()).isEqualTo(7);
    }

    private static IntegrationClaimService service(IntegrationMessageStore store, AuditApplicationService audit) {
        return new IntegrationClaimService(store, new ControlledIntegrationRetryPolicy(), audit,
            Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static InboxMessage withId(InboxMessage source, long id) {
        return new InboxMessage(id, source.organizationId(), source.actorId(), source.sourceSystem(),
            source.messageId(), source.payloadJson(), source.status(), source.receivedAt(), source.processedAt(),
            source.retryCount(), source.nextRetryAt(), source.lastErrorCode(), source.lastErrorMessage(),
            source.versionNo(), source.claimedAt());
    }
}
